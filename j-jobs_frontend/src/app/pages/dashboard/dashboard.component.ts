// src/app/pages/dashboard/dashboard.component.ts
import { CommonModule } from '@angular/common';
import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ContractEnum, JobStatusEnum, LabelsService, OfferStatusEnum, WorkModeEnum, WorkTimeEnum } from '@app/services/labels.service';
import { MenuDataNoTitle, MenuService } from '@app/services/menu.service';
import { PageTitleService } from '@app/services/page-title.service';
import { forkJoin, Subscription } from 'rxjs';
import { StatusStatsService, Stats } from '@app/services/status-stats.service'; 

@Component({
    standalone: true,
    selector: 'app-dashboard.component',
    imports: [CommonModule],
    templateUrl: './dashboard.component.html',
    styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit, OnDestroy {

    private subscription?: Subscription;
    stats: Stats | null = null; 

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

    constructor(
        private labelsService: LabelsService,
        private menuService: MenuService,
        private sanitizer: DomSanitizer,
        private pageTitleService: PageTitleService,
        private statusStatsService: StatusStatsService 
    ) { }

    getIcon(name: string): SafeHtml {
        const iconHtml = this.menuService.getIcon(name);
        return this.sanitizer.bypassSecurityTrustHtml(iconHtml);
    }

    ngOnInit(): void {
        this.pageTitleService.setTitle('Tableau de bord');
        this.menuData = this.menuService.getSideMenuData();
        
        this.subscription = forkJoin([
            this.labelsService.fetchLabels(), // Charge les labels (OfferStatusEnum)
            this.statusStatsService.fetchStats() // Charge les stats (via le nouvel endpoint)
        ]).subscribe({
            next: ([labelsResponse, statsResponse]) => {
                // 1. Mise à jour des Labels (logique existante)
                this.workTimeEnum = this.labelsService.getWorkTimeEnum();
                this.offerStatusEnum = this.labelsService.getOfferStatusEnum();
                this.jobStatusEnum = this.labelsService.getJobStatusEnum();
                this.contractEnum = this.labelsService.getContractEnum();
                this.workModeEnum = this.labelsService.getWorkModeEnum();

                // 2. Mise à jour des Stats
                this.stats = statsResponse;
                this.enCoursCount = this.stats.EN_COURS;
                this.enAttenteCount = this.stats.EN_ATTENTE;
                this.entretienCount = this.stats.ENTRETIEN;
                this.refuseCount = this.stats.REFUSE;
                
                console.log('✅ Dashboard - Labels et Stats chargés:', {
                    labels: labelsResponse,
                    stats: this.stats
                });
            },
            error: (err) => {
                console.error('❌ Erreur lors du chargement des données du tableau de bord:', err);
                // On tente de récupérer les données du cache même en cas d'erreur
                this.stats = this.statusStatsService.getStats();
                this.offerStatusEnum = this.labelsService.getOfferStatusEnum();
            }
        });
    }

    ngOnDestroy(): void {
        this.subscription?.unsubscribe();
        this.pageTitleService.setTitle('');
    }
}
