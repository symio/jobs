// src/app/services/modal.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface ModalConfig {
    title: string;
    message: string;
    type: 'info' | 'success' | 'warning' | 'error' | 'confirm';
    confirmText?: string;
    cancelText?: string;
    showCancel?: boolean;
    confirmType?: string;
}

interface ModalState {
    isOpen: boolean;
    config: ModalConfig | null;
    resolve: ((value: boolean) => void) | null;
}

interface ModalStatusState {
    isOpen: boolean;
    config: ModalConfig | null;
    statusOptions: { [key: string]: string } | null;
    currentStatus: string | null;
    resolve: ((value: { confirmed: boolean; status?: string }) => void) | null;
}

@Injectable({
    providedIn: 'root',
})
export class ModalService {
    private modalState = new BehaviorSubject<ModalState>({
        isOpen: false, 
        config: null, 
        resolve: null,
    });
    
    private modalStatusState = new BehaviorSubject<ModalStatusState>({
        isOpen: false, 
        config: null, 
        statusOptions: null, 
        currentStatus: null, 
        resolve: null,
    });

    public modalState$ = this.modalState.asObservable();
    public modalStatusState$ = this.modalStatusState.asObservable();

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
            title, message,type: 'error', 
            confirmText: 'OK', showCancel: false,
        });
    }

    /**
     * Affiche une modale de confirmation
     */
    confirm(
        title: string,
        message: string,
        confirmText: string = 'Confirmer',
        cancelText: string = 'Annuler',
        confirmType: string = ""
    ): Promise<boolean> {
        return this.showModal({
            title,  message, type: 'confirm',
            confirmText, cancelText,
            showCancel: true, confirmType: confirmType
        });
    }
    
    /**
     * Affiche la modale de changement de statut avec formulaire
     */
    confirmStatus(
        title: string,
        message: string,
        statusOptions: { [key: string]: string },
        currentStatus: string,
        confirmText: string = 'Confirmer',
        cancelText: string = 'Annuler'
    ): Promise<{ confirmed: boolean; status?: string }> {
        return new Promise<{ confirmed: boolean; status?: string }>((resolve) => {
            const config: ModalConfig = {
                title, message, type: 'confirm',
                confirmText, cancelText,
                showCancel: true, confirmType: 'status'
            };
            
            this.modalStatusState.next({
                isOpen: true,config,
                statusOptions, currentStatus, resolve,
            });
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
     * Ferme la modale standard avec une réponse
     */
    close(confirmed: boolean): void {
        const currentState = this.modalState.value;
        if (currentState.resolve) {
            currentState.resolve(confirmed);
        }
        this.modalState.next({
            isOpen: false, config: null,resolve: null,
        });
    }
    
    /**
     * Ferme la modale de statut avec une réponse
     */
    closeStatus(confirmed: boolean, status?: string): void {
        const currentState = this.modalStatusState.value;
        if (currentState.resolve) {
            currentState.resolve({ confirmed, status });
        }
        this.modalStatusState.next({
            isOpen: false, config: null, statusOptions: null,
            currentStatus: null, resolve: null,
        });
    }
}
