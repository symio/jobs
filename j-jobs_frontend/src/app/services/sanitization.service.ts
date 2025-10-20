// src/app/services/sanitization.service.ts
import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root',
})
export class SanitizationService {
    constructor() { }

    sanitizePhase1(value: string | null | undefined): string {
        if (!value) return '';

        return String(value)
            .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
            .replace(/<iframe\b[^<]*(?:(?!<\/iframe>)<[^<]*)*<\/iframe>/gi, '')
            .replace(/<object\b[^<]*(?:(?!<\/object>)<[^<]*)*<\/object>/gi, '')
            .replace(/<embed\b[^<]*(?:(?!<\/embed>)<[^<]*)*<\/embed>/gi, '')
            .replace(/<applet\b[^<]*(?:(?!<\/applet>)<[^<]*)*<\/applet>/gi, '')
            .replace(/<meta\b[^<]*(?:(?!<\/meta>)<[^<]*)*<\/meta>/gi, '')
            .replace(/<link\b[^<]*(?:(?!<\/link>)<[^<]*)*<\/link>/gi, '')
            .replace(/<style\b[^<]*(?:(?!<\/style>)<[^<]*)*<\/style>/gi, '')
            .replace(/on\w+\s*=\s*["'][^"']*["']/gi, '')
            .replace(/on\w+\s*=\s*[^\s>]*/gi, '');
    }

    sanitizePhase2(value: string | null | undefined): string {
        if (!value) return '';

        return String(value)
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#x27;')
            .replace(/\//g, '&#x2F;');
    }

    /**
     * Nettoie une chaîne de caractères en supprimant les balises HTML dangereuses
     * et en échappant les caractères spéciaux
     */
    sanitizeString(value: string | null | undefined): string {
        if (!value) return '';

        return this.sanitizePhase2(
            this.sanitizePhase1(value)
        )
            .trim()
            .replace(/\s+/g, ' ');
    }

    /**
     * Nettoie une chaîne tout en préservant les sauts de ligne
     * Utile pour les textarea
     */
    sanitizeMultiline(value: string | null | undefined): string {
        if (!value) return '';

        const str = String(value);

        const lines = str.split(/\r?\n/);

        const sanitizedLines = lines.map((line) => {
            return this.sanitizePhase2(
                this.sanitizePhase1(line)
            ).trim();

        });

        return sanitizedLines.join('\n');
    }

    /**
     * Nettoie un objet complet en appliquant la sanitization
     * sur toutes les propriétés de type string
     */
    sanitizeObject<T extends Record<string, any>>(
        obj: T,
        multilineFields: string[] = []
    ): T {
        const sanitized = { ...obj };

        (Object.keys(sanitized) as (keyof T)[]).forEach((key) => {
            const value = sanitized[key];

            if (typeof value === 'string') {
                if (multilineFields.includes(key as string)) {
                    sanitized[key] = this.sanitizeMultiline(value) as T[keyof T];
                } else {
                    sanitized[key] = this.sanitizeString(value) as T[keyof T];
                }
            } else if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
                sanitized[key] = this.sanitizeObject(value, multilineFields) as T[keyof T];
            } else if (Array.isArray(value)) {
                sanitized[key] = (value as unknown[]).map((item: unknown) =>
                    typeof item === 'string'
                        ? this.sanitizeString(item)
                        : typeof item === 'object' && item !== null ? this.sanitizeObject(item, multilineFields) : item
                ) as T[keyof T];
            }
        });

        return sanitized;
    }

    /**
     * Valide une URL pour s'assurer qu'elle est sûre
     */
    sanitizeUrl(url: string | null | undefined): string {
        if (!url) return '';

        const str = String(url).trim();

        const dangerousProtocols = [
            'javascript:', 'data:', 'vbscript:', 'file:', 'about:',
        ];

        const lowerUrl = str.toLowerCase();
        for (const protocol of dangerousProtocols) {
            if (lowerUrl.startsWith(protocol)) {
                console.warn(`URL dangereuse détectée et bloquée: ${str}`);
                return '';
            }
        }

        if (
            !str.startsWith('http://') && !str.startsWith('https://') &&
            !str.startsWith('/') && !str.startsWith('#')
        ) {
            return '';
        }

        return str;
    }

    /**
     * Nettoie un email
     */
    sanitizeEmail(email: string | null | undefined): string {
        if (!email) return '';

        const raw = String(email);

        if (/<[^>]*>/.test(raw) || /script/i.test(raw)) {
            return '';
        }

        const cleaned = raw
            .replace(/[<>"']/g, '')
            .trim()
            .toLowerCase();

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(cleaned) ? cleaned : '';
    }

    /**
     * Décode les entités HTML (pour affichage)
     */
    decodeHtml(value: string): string {
        if (!value) return '';

        const textarea = document.createElement('textarea');
        textarea.innerHTML = value;
        return textarea.value;
    }

    /**
     * Vérifie si une chaîne contient du contenu potentiellement dangereux
     */
    containsDangerousContent(value: string | null | undefined): boolean {
        if (!value) return false;

        const str = String(value).toLowerCase();

        const dangerousPatterns = [
            /<script/i, /<iframe/i, /<object/i, /<embed/i,
            /<applet/i, /javascript:/i, /on\w+\s*=/i,
            /data:text\/html/i, /<meta/i, /<link/i, /<style/i,
        ];

        return dangerousPatterns.some((pattern) => pattern.test(str));
    }

    /**
     * Nettoie les données d'un formulaire avant soumission
     */
    sanitizeFormData<T extends Record<string, any>>(
        formData: T,
        config?: {
            multilineFields?: (keyof T)[];
            emailFields?: (keyof T)[];
            urlFields?: (keyof T)[];
            skipFields?: (keyof T)[];
        }
    ): T {
        const {
            multilineFields = [],
            emailFields = [],
            urlFields = [],
            skipFields = [],
        } = config || {};

        const sanitized = { ...formData };

        (Object.keys(sanitized) as (keyof T)[]).forEach((key) => {
            if (skipFields.includes(key)) {
                return;
            }

            const value = sanitized[key];

            if (typeof value === 'string') {
                if (emailFields.includes(key as string)) {
                    sanitized[key] = this.sanitizeEmail(value) as T[keyof T];
                } else if (urlFields.includes(key as string)) {
                    sanitized[key] = this.sanitizeUrl(value) as T[keyof T];
                } else if (multilineFields.includes(key as string)) {
                    sanitized[key] = this.sanitizeMultiline(value) as T[keyof T];
                } else {
                    sanitized[key] = this.sanitizeString(value) as T[keyof T];
                }
            }
        });

        return sanitized;
    }
}
