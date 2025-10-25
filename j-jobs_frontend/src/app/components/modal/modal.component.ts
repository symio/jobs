// src/app/components/modal/modal.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { ModalService, ModalConfig } from '@app/services/modal.service';

@Component({
    selector: 'app-modal',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './modal.component.html',
    styleUrl: './modal.component.scss',
})
export class ModalComponent implements OnInit, OnDestroy {
    isOpen = false;
    config: ModalConfig | null = null;
    
    isStatusOpen = false;
    configStatus: ModalConfig | null = null;
    statusOptions: { [key: string]: string } | null = null;
    selectedStatus: string = '';
    
    private subscription?: Subscription;
    private subscriptionStatus?: Subscription;

    constructor(
        private modalService: ModalService,
        private sanitizer: DomSanitizer
    ) { }

    currentStatusValue: string = '';

    ngOnInit(): void {
        this.subscription = this.modalService.modalState$.subscribe((state) => {
            this.isOpen = state.isOpen;
            this.config = state.config;
        });
        
        this.subscriptionStatus = this.modalService.modalStatusState$.subscribe((state) => {
            this.isStatusOpen = state.isOpen;
            this.configStatus = state.config;
            this.statusOptions = state.statusOptions;
            if (state.isOpen) {
                this.currentStatusValue = state.currentStatus || '';
                this.selectedStatus = '';
            }
        });
    }

    ngOnDestroy(): void {
        this.subscription?.unsubscribe();
        this.subscriptionStatus?.unsubscribe();
    }

    // === MÉTHODES MODALE STANDARD ===
    onConfirm(): void {
        this.modalService.close(true);
    }

    onCancel(): void {
        this.modalService.close(false);
    }

    onBackdropClick(event: MouseEvent): void {
        if (event.target === event.currentTarget) {
            this.onCancel();
        }
    }
    
    // === MÉTHODES MODALE DE STATUT ===
    onConfirmStatus(): void {
        const finalStatus = this.selectedStatus || this.currentStatusValue;
        this.modalService.closeStatus(true, finalStatus);
    }
    
    onCancelStatus(): void {
        this.modalService.closeStatus(false);
    }
    
    onStatusChange(event: Event): void {
        const target = event.target as HTMLSelectElement;
        this.selectedStatus = target.value;
    }
    
    onBackdropClickStatus(event: MouseEvent): void {
        if (event.target === event.currentTarget) {
            this.onCancelStatus();
        }
    }
    
    // === MÉTHODES COMMUNES ===
    getConfirmButtonClasses(): { [key: string]: boolean } {
        const baseClasses: { [key: string]: boolean } = {
            'validate': this.config?.type === 'success' || this.config?.type === 'confirm',
            'delete': this.config?.type === 'error',
            'update': this.config?.type === 'warning',
            'primary': this.config?.type === 'info'
        };

        const dynamicClass = this.getButtonClass('valid');

        if (dynamicClass) { 
            baseClasses[dynamicClass] = true;
        }

        return baseClasses;
    }

    getButtonClass(buttonName: string): string | null {
        const activeConfig = this.isStatusOpen ? this.configStatus : this.config;
        
        if (!activeConfig) return null;

        if (activeConfig.confirmType === "delete") {
            if (buttonName === "cancel")
                return "success";
            else if (buttonName === "valid")
                return "delete";
        }

        return null;
    }
    
    getIconClass(): string {
        const activeConfig = this.isStatusOpen ? this.configStatus : this.config;
        
        if (!activeConfig) return '';

        const iconMap: Record<string, string> = {
            info: 'modal-icon-info',
            success: 'modal-icon-success',
            warning: 'modal-icon-warning',
            error: 'modal-icon-error',
            confirm: 'modal-icon-confirm',
        };

        return iconMap[activeConfig.type] || '';
    }

    getIcon(): SafeHtml {
        const activeConfig = this.isStatusOpen ? this.configStatus : this.config;
        
        if (!activeConfig) return '';

        const icons: Record<string, string> = {
            info: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12,0A12,12,0,1,0,24,12,12.013,12.013,0,0,0,12,0Zm.25,5a1.5,1.5,0,1,1-1.5,1.5A1.5,1.5,0,0,1,12.25,5ZM14.5,18.5h-4a1,1,0,0,1,0-2h.75a.25.25,0,0,0,.25-.25v-4.5a.25.25,0,0,0-.25-.25H10.5a1,1,0,0,1,0-2h1a2,2,0,0,1,2,2v4.75a.25.25,0,0,0,.25.25h.75a1,1,0,1,1,0,2Z"/>
      </svg>`,
            success: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12,0A12,12,0,1,0,24,12,12.014,12.014,0,0,0,12,0Zm6.927,8.2-6.845,9.289a1.011,1.011,0,0,1-1.43.188L5.764,13.769a1,1,0,1,1,1.25-1.562l4.076,3.261,6.227-8.451A1,1,0,1,1,18.927,8.2Z"/>
      </svg>`,
            warning: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12,16a1,1,0,1,0,1,1A1,1,0,0,0,12,16Zm10.67,1.47-8.05-14a3,3,0,0,0-5.24,0l-8,14A3,3,0,0,0,3.94,22H20.06a3,3,0,0,0,2.61-4.53Zm-1.73,2a1,1,0,0,1-.88.51H3.94a1,1,0,0,1-.88-.51,1,1,0,0,1,0-1l8-14a1,1,0,0,1,1.78,0l8.05,14A1,1,0,0,1,20.94,19.49ZM12,8a1,1,0,0,0-1,1v4a1,1,0,0,0,2,0V9A1,1,0,0,0,12,8Z"/>
      </svg>`,
            error: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12,0A12,12,0,1,0,24,12,12.013,12.013,0,0,0,12,0Zm4.707,15.293a1,1,0,1,1-1.414,1.414L12,13.414,8.707,16.707a1,1,0,0,1-1.414-1.414L10.586,12,7.293,8.707A1,1,0,1,1,8.707,7.293L12,10.586l3.293-3.293a1,1,0,1,1,1.414,1.414L13.414,12Z"/>
      </svg>`,
            confirm: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12,0A12,12,0,1,0,24,12,12.013,12.013,0,0,0,12,0Zm.25,5a1.5,1.5,0,1,1-1.5,1.5A1.5,1.5,0,0,1,12.25,5ZM14.5,18.5h-4a1,1,0,0,1,0-2h.75a.25.25,0,0,0,.25-.25v-4.5a.25.25,0,0,0-.25-.25H10.5a1,1,0,0,1,0-2h1a2,2,0,0,1,2,2v4.75a.25.25,0,0,0,.25.25h.75a1,1,0,1,1,0,2Z"/>
      </svg>`,
        };

        const iconHtml = icons[activeConfig.type] || icons['info'];
        return this.sanitizer.bypassSecurityTrustHtml(iconHtml);
    }
}
