// src/app/pages/register/activate/activate.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Observable } from 'rxjs';
import { UserService } from '@app/services/user.service';
import { OAuth2Service } from '@app/services/oauth2.service';
import { SanitizationService } from '@app/services/sanitization.service';
import { AbstractKeyPageComponent } from '@components/abstract-key-page/abstract-key-page.component'; 

@Component({
    standalone: true,
    selector: 'app-passwordlost_new-password',
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: '../../../../components/abstract-key-page/abstract-key-page.component.html',
    styleUrl: '../../../../components/abstract-key-page/abstract-key-page.component.scss' 
})
export class DeactivateStep2Component extends AbstractKeyPageComponent {
    mainTitle = "Désactivation du compte"; 
    
    constructor(
        route: ActivatedRoute,
        formBuilder: FormBuilder,
        userService: UserService,
        oAuth2Service: OAuth2Service,
        sanitizationService: SanitizationService,
        router: Router
    ) {
        super(route, formBuilder, userService, oAuth2Service, sanitizationService, router);
    }

    protected processKey(key: string): Observable<void> {
        return this.userService.deactivatePasswordStep2(key); 
    }
}