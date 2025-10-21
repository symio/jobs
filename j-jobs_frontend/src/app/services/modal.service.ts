// src/app/services/modal.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface ModalConfig {
    title: string;
    message: string;
    type: 'info' | 'success' | 'warning' | 'error' | 'confirm';
    confirmText?: string;
    cancelText?: string;
    showCancel?: boolean;
}

interface ModalState {
    isOpen: boolean;
    config: ModalConfig | null;
    resolve: ((value: boolean) => void) | null;
}

@Injectable({
    providedIn: 'root',
})
export class ModalService {
    private modalState = new BehaviorSubject<ModalState>({
        isOpen: false, config: null, resolve: null,
    });

    public modalState$ = this.modalState.asObservable();

    /**
     * Affiche une modale d'information
     */
    info(title: string, message: string): Promise<boolean> {
        return this.showModal({
            title, message, type: 'info',
            confirmText: 'OK', showCancel: false,
        });
    }

    /**
     * Affiche une modale de succès
     */
    success(title: string, message: string): Promise<boolean> {
        return this.showModal({
            title, message, type: 'success',
            confirmText: 'OK', showCancel: false,
        });
    }

    /**
     * Affiche une modale d'avertissement
     */
    warning(title: string, message: string): Promise<boolean> {
        return this.showModal({
            title, message, type: 'warning',
            confirmText: 'OK', showCancel: false,
        });
    }

    /**
     * Affiche une modale d'erreur
     */
    error(title: string, message: string): Promise<boolean> {
        return this.showModal({
            title, message,
            type: 'error', confirmText: 'OK',
            showCancel: false,
        });
    }

    /**
     * Affiche une modale de confirmation
     */
    confirm(
        title: string,
        message: string,
        confirmText: string = 'Confirmer',
        cancelText: string = 'Annuler'
    ): Promise<boolean> {
        return this.showModal({
            title, message, type: 'confirm',
            confirmText, cancelText,
            showCancel: true,
        });
    }

    /**
     * Méthode générique pour afficher une modale
     */
    private showModal(config: ModalConfig): Promise<boolean> {
        return new Promise<boolean>((resolve) => {
            this.modalState.next({isOpen: true, config, resolve,});
        });
    }

    /**
     * Ferme la modale avec une réponse
     */
    close(confirmed: boolean): void {
        const currentState = this.modalState.value;
        if (currentState.resolve) {
            currentState.resolve(confirmed);
        }
        this.modalState.next({isOpen: false, config: null,resolve: null,});
    }
}
