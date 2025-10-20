import { Component, OnDestroy, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
    FormBuilder, FormGroup,
    ReactiveFormsModule, Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import {
    ContractEnum, JobStatusEnum, LabelsService,
    OfferStatusEnum, WorkModeEnum, WorkTimeEnum,
} from '@app/services/labels.service';
import { MenuDataNoTitle, MenuService } from '@app/services/menu.service';
import { PageTitleService } from '@app/services/page-title.service';
import { catchError, forkJoin, of, Subscription } from 'rxjs';
import { JobsService, Job, CreateJobRequest, UpdateJobRequest } from '@app/services/jobs.service';
import { SanitizationService } from '@app/services/sanitization.service';

@Component({
    standalone: true,
    selector: 'app-job-form-component',
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './job-form.component.html',
    styleUrl: './job-form.component.scss',
})
export class JobFormComponent implements OnInit, OnDestroy {
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
    form!: FormGroup;
    isLoading = true;
    isSubmitting = false;
    error: string | null = null;
    titreSuffix: string = '';

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private labelsService: LabelsService,
        private menuService: MenuService,
        private sanitizer: DomSanitizer,
        private pageTitleService: PageTitleService,
        private jobsService: JobsService,
        private fb: FormBuilder,
        private sanitizationService: SanitizationService
    ) { }

    ngOnInit(): void {
        this.link = this.route.snapshot.queryParamMap.get('link');
        console.log('Link reçu :', this.link);

        setTimeout(() => {
            this.pageTitleService.setTitle("Formulaire offre d'emploi");
        }, 0);

        this.menuData = this.menuService.getSideMenuData();

        if (this.link) {
            this.subscription = forkJoin([
                this.labelsService.fetchLabels(),
                this.jobsService.getJobByHalLink(this.link).pipe(
                    catchError((err) => {
                        console.error("Erreur de chargement de l'offre", err);
                        this.error = "Impossible de charger l'offre. Veuillez réessayer.";
                        return of(null);
                    }),
                ),
            ]).subscribe({
                next: ([labelsResponse, job]) => {
                    this.applyLabelsAndTitle(job ?? undefined);
                },
                error: (err) => {
                    this.offerStatusEnum = this.labelsService.getOfferStatusEnum();
                    this.isLoading = false;
                    this.error = 'Erreur lors du chargement des données.';
                },
            });
        } else {
            this.subscription = this.labelsService.fetchLabels().subscribe({
                next: () => {
                    this.applyLabelsAndTitle(undefined);
                },
                error: (err) => {
                    this.isLoading = false;
                    this.error = 'Erreur lors du chargement des données.';
                },
            });
        }
    }

    private applyLabelsAndTitle(job?: Job): void {
        this.workTimeEnum = this.labelsService.getWorkTimeEnum();
        this.offerStatusEnum = this.labelsService.getOfferStatusEnum();
        this.jobStatusEnum = this.labelsService.getJobStatusEnum();
        this.contractEnum = this.labelsService.getContractEnum();
        this.workModeEnum = this.labelsService.getWorkModeEnum();

        this.isLoading = false;
        this.job = job ?? null;
        this.initForm(job ?? null);

        const titre = job ? "Modifier l'offre" : "Créer une offre d'emploi";

        setTimeout(() => this.pageTitleService.setTitle(titre), 0);
    }

    ngOnDestroy(): void {
        this.subscription?.unsubscribe();
        this.pageTitleService.setTitle('');
    }

    private initForm(job: Job | null): void {
        this.form = this.fb.group({
            position: [
                job?.position ? this.sanitizationService.decodeHtml(job.position) : '',
                Validators.required
            ],
            compagny: [
                job?.compagny ? this.sanitizationService.decodeHtml(job.compagny) : '',
                Validators.required
            ],
            city: [
                job?.city ? this.sanitizationService.decodeHtml(job.city) : '',
                Validators.required
            ],
            contract: [job?.contract || ''],
            workTime: [job?.workTime || ''],
            workMode: [job?.workMode || ''],
            offerStatus: [job?.offerStatus || ''],
            from_official_dom: [job?.from_official_dom || false],
            description: [
                job?.description ? this.sanitizationService.decodeHtml(job.description) : ''
            ],
        });
    }

    onSubmit(): void {
        if (this.form.invalid) {
            Object.keys(this.form.controls).forEach((key) => {
                this.form.get(key)?.markAsTouched();
            });
            return;
        }

        this.isSubmitting = true;
        const formValue = this.form.value;

        const sanitizedData = this.sanitizationService.sanitizeFormData(formValue, {
            multilineFields: ['description'], skipFields: ['from_official_dom'],
        });

        const hasDangerousContent = Object.entries(sanitizedData).some(([key, value]) => {
            if (typeof value === 'string') {
                const isDangerous = this.sanitizationService.containsDangerousContent(value);
                if (isDangerous) {
                    console.warn(`Contenu dangereux détecté dans le champ: ${key}`);
                }
                return isDangerous;
            }
            return false;
        });

        if (hasDangerousContent) {
            alert(
                'Des contenus potentiellement dangereux ont été détectés dans votre formulaire. ' +
                'Veuillez vérifier vos données et réessayer.'
            );
            this.isSubmitting = false;
            return;
        }

        // Envoi des données nettoyées
        if (this.job && this.link) {
            this.jobsService.updateJob(this.link, sanitizedData as UpdateJobRequest).subscribe({
                next: () => {
                    alert('Offre mise à jour avec succès !');
                    this.router.navigate(['/dashboard']);
                },
                error: (err) => {
                    console.error('Erreur lors de la mise à jour', err);
                    alert("Erreur lors de la mise à jour de l'offre.");
                    this.isSubmitting = false;
                },
            });
        } else {
            this.jobsService.createJob(sanitizedData as CreateJobRequest).subscribe({
                next: () => {
                    alert('Offre créée avec succès !');
                    this.router.navigate(['/dashboard']);
                },
                error: (err) => {
                    console.error('Erreur lors de la création', err);
                    alert("Erreur lors de la création de l'offre.");
                    this.isSubmitting = false;
                },
            });
        }
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

    get city() {
        return this.form.get('city');
    }
    get compagny() {
        return this.form.get('compagny');
    }
    get contract() {
        return this.form.get('contract');
    }
    get description() {
        return this.form.get('description');
    }
    get from_official_dom() {
        return this.form.get('from_official_dom');
    }
    get offerStatus() {
        return this.form.get('offerStatus');
    }
    get position() {
        return this.form.get('position');
    }
    get workMode() {
        return this.form.get('workMode');
    }
    get workTime() {
        return this.form.get('workTime');
    }
}
