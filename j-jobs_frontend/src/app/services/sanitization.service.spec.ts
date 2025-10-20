// src/app/services/sanitization.service.spec.ts
import { TestBed } from '@angular/core/testing';
import { SanitizationService } from './sanitization.service';

describe('SanitizationService', () => {
    let service: SanitizationService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(SanitizationService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('sanitizeString', () => {
        it('should remove script tags', () => {
            const input = 'Hello <script>alert("XSS")</script> World';
            const result = service.sanitizeString(input);
            expect(result).not.toContain('<script');
            expect(result).not.toContain('alert');
        });

        it('should escape HTML characters', () => {
            const input = '<div>Test</div>';
            const result = service.sanitizeString(input);
            expect(result).toBe('&lt;div&gt;Test&lt;&#x2F;div&gt;');
        });

        it('should remove inline event handlers', () => {
            const input = '<img src="x" onerror="alert(1)">';
            const result = service.sanitizeString(input);
            expect(result).not.toContain('onerror');
        });

        it('should handle null and undefined', () => {
            expect(service.sanitizeString(null)).toBe('');
            expect(service.sanitizeString(undefined)).toBe('');
        });

        it('should trim and normalize spaces', () => {
            const input = '  Hello    World  ';
            const result = service.sanitizeString(input);
            expect(result).toBe('Hello World');
        });
    });

    describe('sanitizeMultiline', () => {
        it('should preserve line breaks', () => {
            const input = 'Line 1\nLine 2\nLine 3';
            const result = service.sanitizeMultiline(input);
            expect(result).toContain('\n');
            expect(result.split('\n').length).toBe(3);
        });

        it('should remove scripts from multiline text', () => {
            const input = 'Line 1\n<script>alert("XSS")</script>\nLine 3';
            const result = service.sanitizeMultiline(input);
            expect(result).not.toContain('<script');
        });

        it('should handle Windows line breaks (CRLF)', () => {
            const input = 'Line 1\r\nLine 2\r\nLine 3';
            const result = service.sanitizeMultiline(input);
            const lines = result.split('\n');
            expect(lines.length).toBe(3);
        });
    });

    describe('sanitizeEmail', () => {
        it('should accept valid emails', () => {
            const validEmails = [
                'test@example.com',
                'user.name@domain.co.uk',
                'user+tag@example.com',
            ];

            validEmails.forEach((email) => {
                const result = service.sanitizeEmail(email);
                expect(result).toBeTruthy();
                expect(result).not.toBe('');
            });
        });

        it('should reject invalid emails', () => {
            const invalidEmails = [
                'notanemail',
                '@example.com',
                'user@',
                'user @example.com',
                '<script>@example.com',
            ];

            invalidEmails.forEach((email) => {
                const result = service.sanitizeEmail(email);
                expect(result).toBe('');
            });
        });

        it('should remove dangerous characters from valid emails', () => {
            const input = 'test@example.com<script>';
            const result = service.sanitizeEmail(input);
            expect(result).toBe('');
        });

        it('should convert to lowercase', () => {
            const input = 'User@Example.COM';
            const result = service.sanitizeEmail(input);
            expect(result).toBe('user@example.com');
        });
    });

    describe('sanitizeUrl', () => {
        it('should accept safe URLs', () => {
            const safeUrls = [
                'http://example.com',
                'https://example.com',
                '/path/to/page',
                '#anchor',
            ];

            safeUrls.forEach((url) => {
                const result = service.sanitizeUrl(url);
                expect(result).toBe(url);
            });
        });

        it('should block dangerous protocols', () => {
            const dangerousUrls = [
                'javascript:alert(1)',
                'data:text/html,<script>alert(1)</script>',
                'vbscript:alert(1)',
                'file:///etc/passwd',
            ];

            dangerousUrls.forEach((url) => {
                const result = service.sanitizeUrl(url);
                expect(result).toBe('');
            });
        });

        it('should handle case-insensitive protocols', () => {
            const input = 'JavaScript:alert(1)';
            const result = service.sanitizeUrl(input);
            expect(result).toBe('');
        });
    });

    describe('containsDangerousContent', () => {
        it('should detect script tags', () => {
            const input = 'Hello <script>alert(1)</script>';
            expect(service.containsDangerousContent(input)).toBe(true);
        });

        it('should detect iframe tags', () => {
            const input = '<iframe src="evil.com"></iframe>';
            expect(service.containsDangerousContent(input)).toBe(true);
        });

        it('should detect inline event handlers', () => {
            const input = '<img onerror="alert(1)">';
            expect(service.containsDangerousContent(input)).toBe(true);
        });

        it('should return false for safe content', () => {
            const input = 'This is safe content';
            expect(service.containsDangerousContent(input)).toBe(false);
        });

        it('should handle null and undefined', () => {
            expect(service.containsDangerousContent(null)).toBe(false);
            expect(service.containsDangerousContent(undefined)).toBe(false);
        });
    });

    describe('sanitizeObject', () => {
        it('should sanitize all string properties', () => {
            const input = {
                name: '<script>alert(1)</script>John',
                age: 30,
                active: true,
            };

            const result = service.sanitizeObject(input);
            expect(result.name).not.toContain('<script');
            expect(result.age).toBe(30);
            expect(result.active).toBe(true);
        });

        it('should handle nested objects', () => {
            const input = {
                user: {
                    name: '<script>Test</script>',
                    email: 'test@example.com',
                },
            };

            const result = service.sanitizeObject(input);
            expect(result.user.name).not.toContain('<script');
        });

        it('should handle arrays', () => {
            const input = {
                tags: ['<script>tag1</script>', 'tag2', 'tag3'],
            };

            const result = service.sanitizeObject(input);
            expect(result.tags[0]).not.toContain('<script');
            expect(result.tags[1]).toBe('tag2');
        });

        it('should respect multilineFields', () => {
            const input = {
                title: 'Line 1\nLine 2',
                description: 'Line A\nLine B',
            };

            const result = service.sanitizeObject(input, ['description']);
            expect(result.description).toContain('\n');
        });
    });

    describe('sanitizeFormData', () => {
        it('should sanitize form data with proper configuration', () => {
            const formData = {
                name: '<script>John</script>',
                email: 'test@example.com',
                description: 'Line 1\nLine 2',
                password: 'P@ssw0rd!',
                isActive: true,
            };

            const result = service.sanitizeFormData(formData, {
                multilineFields: ['description'],
                emailFields: ['email'],
                skipFields: ['password', 'isActive'],
            });

            expect(result.name).not.toContain('<script');
            expect(result.email).toBe('test@example.com');
            expect(result.description).toContain('\n');
            expect(result.password).toBe('P@ssw0rd!');
            expect(result.isActive).toBe(true);
        });

        it('should return empty string for invalid email', () => {
            const formData = {
                email: 'invalid-email<script>',
            };

            const result = service.sanitizeFormData(formData, {
                emailFields: ['email'],
            });

            expect(result.email).toBe('');
        });

        it('should return empty string for dangerous URL', () => {
            const formData = {
                website: 'javascript:alert(1)',
            };

            const result = service.sanitizeFormData(formData, {
                urlFields: ['website'],
            });

            expect(result.website).toBe('');
        });
    });

    describe('decodeHtml', () => {
        it('should decode HTML entities', () => {
            const input = '&lt;div&gt;Test&lt;&#x2F;div&gt;';
            const result = service.decodeHtml(input);
            expect(result).toBe('<div>Test</div>');
        });

        it('should decode common entities', () => {
            const input = '&quot;Hello&quot; &amp; &lt;World&gt;';
            const result = service.decodeHtml(input);
            expect(result).toBe('"Hello" & <World>');
        });

        it('should handle empty input', () => {
            expect(service.decodeHtml('')).toBe('');
            expect(service.decodeHtml(null as any)).toBe('');
        });
    });
});
