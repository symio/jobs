import { Component, OnDestroy, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { forkJoin, Subscription, tap } from 'rxjs';
import {
    ContractEnum,
    JobStatusEnum,
    LabelsService,
    OfferStatusEnum,
    WorkModeEnum,
    WorkTimeEnum,
} from '@app/services/labels.service';
import { MenuDataNoTitle, MenuService } from '@app/services/menu.service';
import { PageTitleService } from '@app/services/page-title.service';
import {
    JobsService,
    Job,
    GetJobsResponse,
    PageInfo,
    SearchJobsRequest,
} from '@app/services/jobs.service';
import { SanitizationService } from '@app/services/sanitization.service';

@Component({
    standalone: true,
    selector: 'app-job-details',
    imports: [CommonModule],
    templateUrl: './job-details.component.html',
    styleUrl: './job-details.component.scss',
})
export class JobDetailsComponent implements OnInit, OnDestroy {
    private subscription?: Subscription;

    workTimeEnum: WorkTimeEnum | null = null;
    offerStatusEnum: OfferStatusEnum | null = null;
    jobStatusEnum: JobStatusEnum | null = null;
    contractEnum: ContractEnum | null = null;
    workModeEnum: WorkModeEnum | null = null;
    menuData: MenuDataNoTitle = { items: [] };

    @Input() showNavMenu: boolean = false;

    link: string | null = null;
    job: Job | null = null;
    isLoading = true;
    error: string | null = null;
    constructor(
        private route: ActivatedRoute,
        private labelsService: LabelsService,
        private menuService: MenuService,
        private sanitizer: DomSanitizer,
        private pageTitleService: PageTitleService,
        private jobsService: JobsService,
        private router: Router,
        private sanitizationService: SanitizationService
    ) { }

    decodeHtml(value: string): string {
        return this.sanitizationService.decodeHtml(value);
    }

    decodeAndPreserveLineBreaks(value: string): string {
        const decoded = this.sanitizationService.decodeHtml(value);
        return decoded.replace(/\n/g, '<br>');
    }

    ngOnInit(): void {
        this.link = this.route.snapshot.queryParamMap.get('link');
        console.log('Link reçu :', this.link);

        setTimeout(() => {
            this.pageTitleService.setTitle("Détails de l'offre");
        }, 0);

        this.menuData = this.menuService.getSideMenuData();

        this.subscription = forkJoin([
            this.labelsService.fetchLabels(),
            this.jobsService.getJobByHalLink(this.link ?? ''),
        ]).subscribe({
            next: ([labelsResponse, job]) => {
                this.workTimeEnum = this.labelsService.getWorkTimeEnum();
                this.offerStatusEnum = this.labelsService.getOfferStatusEnum();
                this.jobStatusEnum = this.labelsService.getJobStatusEnum();
                this.contractEnum = this.labelsService.getContractEnum();
                this.workModeEnum = this.labelsService.getWorkModeEnum();

                this.job = job;
                this.isLoading = false;
            },
            error: (err) => {
                this.offerStatusEnum = this.labelsService.getOfferStatusEnum();
                this.isLoading = false;
                alert('Erreur: Offre non trouvé.');
                this.router.navigate(['/dashboard']);
            },
        });
    }

    ngOnDestroy(): void {
        this.subscription?.unsubscribe();
        this.pageTitleService.setTitle('');
    }

    onUpdateJob(job: any): void {
        let href = job?._links?.self?.href || job?._links?.job?.href;
        if (!href) return;

        const link = href.replace(/^(https?:)?\/\/[^/]+/, '');

        this.router.navigate(['/dashboard/job-form'], { queryParams: { link } });
    }

    onDeleteJob(job: any): void {
        let href = job?._links?.self?.href || job?._links?.job?.href;

        if (!href) {
            console.error(
                "Impossible de trouver le lien de suppression pour l'offre.",
            );
            alert('Erreur: Lien de suppression non trouvé.');
            return;
        }

        const confirmation = confirm(
            `Êtes-vous sûr de vouloir supprimer l'offre : "${job.position}" (${job.compagny}) ? Cette action est irréversible.`,
        );

        if (confirmation) {
            const linkToDelete = href.replace(/^(https?:)?\/\/[^/]+/, '');

            this.jobsService.deleteJob(linkToDelete).subscribe({
                next: () => {
                    alert(`L'offre "${job.position}" a été supprimée avec succès.`);
                    this.router.navigate(['/dashboard']);
                },
                error: (err) => {
                    console.error("Erreur lors de la suppression de l'offre:", err);
                    alert(
                        "Erreur lors de la suppression de l'offre. Veuillez réessayer.",
                    );
                },
            });
        }
    }

    getIcon(name: string): SafeHtml {
        const iconHtml = this.menuService.getIcon(name);
        return this.sanitizer.bypassSecurityTrustHtml(iconHtml);
    }

    getOfferStatusIcon(offerStatus: string): SafeHtml {
        const icons: Record<string, string> = {
            A_EN_COURS: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
            <path d="M19,4H17.9A5.009,5.009,0,0,0,13,0H11A5.009,5.009,0,0,0,6.1,4H5A5.006,5.006,0,0,0,0,9v3H24V9A5.006,5.006,0,0,0,19,4ZM8.184,4A3,3,0,0,1,11,2h2a3,3,0,0,1,2.816,2Z"/>
            <path d="M13,15a1,1,0,0,1-2,0V14H0v5a5.006,5.006,0,0,0,5,5H19a5.006,5.006,0,0,0,5-5V14H13Z"/>
        </svg>`,
            O_ACCEPT: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
            <path d="M19,4H17.9A5.009,5.009,0,0,0,13,0H11A5.009,5.009,0,0,0,6.1,4H5A5.006,5.006,0,0,0,0,9v3H24V9A5.006,5.006,0,0,0,19,4ZM8.184,4A3,3,0,0,1,11,2h2a3,3,0,0,1,2.816,2Z"/>
            <path d="M13,15a1,1,0,0,1-2,0V14H0v5a5.006,5.006,0,0,0,5,5H19a5.006,5.006,0,0,0,5-5V14H13Z"/>
        </svg>`,
            B_EN_ATTENTE: `<svg xmlns="http://www.w3.org/2000/svg" data-name="Layer 1" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12,24C5.383,24,0,18.617,0,12S5.383,0,12,0s12,5.383,12,12-5.383,12-12,12Zm5-13h-4V5h-2V13h6v-2Z"/>
        </svg>`,
            B_RELANCE_A: `<svg xmlns="http://www.w3.org/2000/svg" data-name="Layer 1" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12,24C5.383,24,0,18.617,0,12S5.383,0,12,0s12,5.383,12,12-5.383,12-12,12Zm5-13h-4V5h-2V13h6v-2Z"/>
        </svg>`,
            B_RELANCE_E: `<svg xmlns="http://www.w3.org/2000/svg" data-name="Layer 1" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12,24C5.383,24,0,18.617,0,12S5.383,0,12,0s12,5.383,12,12-5.383,12-12,12Zm5-13h-4V5h-2V13h6v-2Z"/>
        </svg>`,
            D_ENTRETIEN: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
            <path d="M0,19a5.006,5.006,0,0,0,5,5H19a5.006,5.006,0,0,0,5-5V10H0Zm17-4.5A1.5,1.5,0,1,1,15.5,16,1.5,1.5,0,0,1,17,14.5Zm-5,0A1.5,1.5,0,1,1,10.5,16,1.5,1.5,0,0,1,12,14.5Zm-5,0A1.5,1.5,0,1,1,5.5,16,1.5,1.5,0,0,1,7,14.5Z"/>
            <path d="M19,2H18V1a1,1,0,0,0-2,0V2H8V1A1,1,0,0,0,6,1V2H5A5.006,5.006,0,0,0,0,7V8H24V7A5.006,5.006,0,0,0,19,2Z"/>
        </svg>`,
            C_REFUSE: `<svg xmlns="http://www.w3.org/2000/svg" data-name="Layer 1" viewBox="0 0 24 24" fill="currentColor">
            <path d="M17,4V2a2,2,0,0,0-2-2H9A2,2,0,0,0,7,2V4H2V6H4V21a3,3,0,0,0,3,3H17a3,3,0,0,0,3-3V6h2V4ZM11,17H9V11h2Zm4,0H13V11h2ZM15,4H9V2h6Z"/>
        </svg>`,
            O_REFUS: `<svg xmlns="http://www.w3.org/2000/svg" data-name="Layer 1" viewBox="0 0 24 24" fill="currentColor">
            <path d="M17,4V2a2,2,0,0,0-2-2H9A2,2,0,0,0,7,2V4H2V6H4V21a3,3,0,0,0,3,3H17a3,3,0,0,0,3-3V6h2V4ZM11,17H9V11h2Zm4,0H13V11h2ZM15,4H9V2h6Z"/>
        </svg>`,
        };

        const iconHtml = icons[offerStatus] || icons['A_EN_COURS'];
        return this.sanitizer.bypassSecurityTrustHtml(iconHtml);
    }

    getOfferStatusClass(offerStatus: string): string {
        const classes: Record<string, string> = {
            A_EN_COURS: 'status-yellow',
            O_ACCEPT: 'status-yellow',
            B_EN_ATTENTE: 'status-green',
            B_RELANCE_A: 'status-yellow',
            B_RELANCE_E: 'status-green',
            D_ENTRETIEN: 'status-blue',
            C_REFUSE: 'status-red',
            O_REFUS: 'status-red',
        };

        return classes[offerStatus] || 'status-yellow';
    }

    getContractLabel(key: string): string {
        return this.labelsService.getContractEnum()?.[key] || key;
    }

    getWorkTimeLabel(key: string): string {
        return this.labelsService.getWorkTimeEnum()?.[key] || key;
    }

    getWorkModeLabel(key: string): string {
        return this.labelsService.getWorkModeEnum()?.[key] || key;
    }

    getOfferStatusLabel(key: string): string {
        return this.labelsService.getOfferStatusEnum()?.[key] || key;
    }

    getJobStatusLabel(key: string): string {
        return this.labelsService.getJobStatusEnum()?.[key] || key;
    }

    getContractKeys(): string[] {
        return this.contractEnum ? Object.keys(this.contractEnum) : [];
    }

    getWorkTimeKeys(): string[] {
        return this.workTimeEnum ? Object.keys(this.workTimeEnum) : [];
    }

    getWorkModeKeys(): string[] {
        return this.workModeEnum ? Object.keys(this.workModeEnum) : [];
    }

    getOfferStatusKeys(): string[] {
        return this.offerStatusEnum ? Object.keys(this.offerStatusEnum) : [];
    }
}
