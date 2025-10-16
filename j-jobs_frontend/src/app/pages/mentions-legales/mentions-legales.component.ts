import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';

@Component({
    selector: 'app-mentions-legales',
    imports: [CommonModule, RouterModule],
    templateUrl: './mentions-legales.component.html',
    styleUrl: './mentions-legales.component.scss'
})
export class MentionsLegalesComponent {
    constructor(private router: Router) {}

    connectUser(): void {
        this.router.navigate(['/login']);
    }

    registerUser(): void {
        this.router.navigate(['/register']);
    }
}
