// src/app/pages/dashboard/dashboard.component.ts
import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  Input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import {
  ContractEnum,
  JobStatusEnum,
  LabelsService,
  OfferStatusEnum,
  WorkModeEnum,
  WorkTimeEnum,
} from '@app/services/labels.service';
import { forkJoin, Subscription, tap } from 'rxjs';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { PageTitleService } from '@app/services/page-title.service';
import { MenuDataNoTitle, MenuService } from '@app/services/menu.service';
import { StatusStatsService, Stats } from '@app/services/status-stats.service';
import {
  JobsService,
  Job,
  GetJobsResponse,
  PageInfo,
  SearchJobsRequest,
} from '@app/services/jobs.service';

@Component({
  standalone: true,
  selector: 'app-dashboard.component',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit, OnDestroy {
  private subscription?: Subscription;
  stats: Stats | null = null;

  searchForm!: FormGroup;
  isSearchOpen: boolean = false;

  workTimeEnum: WorkTimeEnum | null = null;
  offerStatusEnum: OfferStatusEnum | null = null;
  jobStatusEnum: JobStatusEnum | null = null;
  contractEnum: ContractEnum | null = null;
  workModeEnum: WorkModeEnum | null = null;
  menuData: MenuDataNoTitle = { items: [] };

  enCoursCount: number = 0;
  enAttenteCount: number = 0;
  entretienCount: number = 0;
  refuseCount: number = 0;

  @Input() showNavMenu: boolean = false;

  jobs: Job[] | null = null;
  pageInfos: PageInfo | null = null;
  currentPage: number = 0;
  isLoading = true;
  error: string | null = null;

  isSearchMode: boolean = false;

  constructor(
    private formBuilder: FormBuilder,
    private labelsService: LabelsService,
    private menuService: MenuService,
    private sanitizer: DomSanitizer,
    private pageTitleService: PageTitleService,
    private statusStatsService: StatusStatsService,
    private jobsService: JobsService,
    private cdRef: ChangeDetectorRef,
    private router: Router,
  ) {}

  getIcon(name: string): SafeHtml {
    const iconHtml = this.menuService.getIcon(name);
    return this.sanitizer.bypassSecurityTrustHtml(iconHtml);
  }

  ngOnInit(): void {
    this.searchForm = this.formBuilder.group({
      contract: [null, []],
      eventAfter: [null, []],
      eventBefore: [null, []],
      officialdom: [false, []],
      offerStatus: [null, []],
      sort: ['DATE_DESC', []],
      textual: [null, []],
      workMode: [null, []],
      workTime: [null, []],
    });

    setTimeout(() => {
      this.pageTitleService.setTitle('Tableau de bord');
    }, 0);

    this.menuData = this.menuService.getSideMenuData();
    this.subscription = new Subscription();
    this.subscription.add(
      this.jobsService.jobsUpdated$.subscribe(() => {
        this.fetchJobs();
      }),
    );

    this.subscription.add(
      forkJoin([
        this.labelsService.fetchLabels(),
        this.statusStatsService.fetchStats(),
      ]).subscribe({
        next: ([labelsResponse, statsResponse]) => {
          this.workTimeEnum = this.labelsService.getWorkTimeEnum();
          this.offerStatusEnum = this.labelsService.getOfferStatusEnum();
          this.jobStatusEnum = this.labelsService.getJobStatusEnum();
          this.contractEnum = this.labelsService.getContractEnum();
          this.workModeEnum = this.labelsService.getWorkModeEnum();

          this.stats = statsResponse;
          this.enCoursCount = this.stats.EN_COURS;
          this.enAttenteCount = this.stats.EN_ATTENTE;
          this.entretienCount = this.stats.ENTRETIEN;
          this.refuseCount = this.stats.REFUSE;

          setTimeout(() => {
            this.fetchJobs();
          }, 1000);
        },
        error: (err) => {
          this.stats = this.statusStatsService.getStats();
          this.offerStatusEnum = this.labelsService.getOfferStatusEnum();
        },
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
    this.pageTitleService.setTitle('');
  }

  fetchJobs(): void {
    this.isLoading = true;
    this.error = null;

    if (this.isSearchMode) {
      this.performSearch();
    } else {
      this.jobsService.getJobs(this.currentPage, 3).subscribe({
        next: (response: GetJobsResponse) => {
          this.handleJobsResponse(response);
        },
        error: (err) => {
          this.handleJobsError(err);
        },
      });
    }
  }

  performSearch(): void {
    const formValue = this.searchForm.value;

    const searchRequest: SearchJobsRequest = {
      contract: formValue.contract || null,
      eventBefore: formValue.eventBefore || null,
      eventAfter: formValue.eventAfter || null,
      officialdom: formValue.officialdom || false,
      offerStatus: formValue.offerStatus || null,
      sort: formValue.sort || 'DATE_DESC',
      textual: formValue.textual || null,
      workTime: formValue.workTime || null,
      workMode: formValue.workMode || null,
    };

    this.jobsService.searchJobs(searchRequest, this.currentPage, 3).subscribe({
      next: (response: GetJobsResponse) => {
        this.handleJobsResponse(response);
      },
      error: (err) => {
        this.handleJobsError(err);
      },
    });
  }

  private handleJobsResponse(response: GetJobsResponse): void {
    if (response._embedded?.jobs?.length > 0) {
      this.jobs = response._embedded.jobs;
      console.log('jobs : ', this.jobs);
      this.pageInfos = response.page;
    } else {
      this.error = 'Aucune offre trouvée';
      this.jobs = [];
    }
    this.isLoading = false;
  }

  private handleJobsError(err: any): void {
    console.error('[DashboardComponent] Erreur lors du fetch des jobs:', err);
    this.error = 'Erreur lors du chargement des jobs';
    this.isLoading = false;
  }

  onSearch(): void {
    console.log('Recherche lancée avec:', this.searchForm.value);
    this.isSearchMode = true;
    this.currentPage = 0;
    this.fetchJobs();
  }

  onResetSearch(): void {
    this.searchForm.reset({
      contract: null,
      eventAfter: null,
      eventBefore: null,
      officialdom: false,
      offerStatus: null,
      sort: 'DATE_DESC',
      textual: null,
      workMode: null,
      workTime: null,
    });
    this.isSearchMode = false;
    this.currentPage = 0;
    this.fetchJobs();
  }

  onViewJob(job: any): void {
    let href = job?._links?.self?.href || job?._links?.job?.href;
    if (!href) return;

    const link = href.replace(/^(https?:)?\/\/[^/]+/, '');

    this.router.navigate(['/dashboard/details'], { queryParams: { link } });
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

  toggleSearch(): void {
    this.isSearchOpen = !this.isSearchOpen;
  }

  get pages(): any[] {
    const total = this.totalPages;
    if (total <= 1) return [];

    const current = this.currentPage;
    const delta = 1;
    const range: (number | string)[] = [];

    range.push(0);

    if (current - delta > 1) {
      range.push('...');
    }

    for (let i = current - delta; i <= current + delta; i++) {
      if (i > 0 && i < total - 1) {
        range.push(i);
      }
    }

    if (current + delta < total - 2) {
      range.push('...');
    }

    if (total > 1) {
      range.push(total - 1);
    }

    return range;
  }

  goToPage(page: number) {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.fetchJobs();
  }

  get totalPages(): number {
    return this.pageInfos?.total_pages ?? 0;
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
