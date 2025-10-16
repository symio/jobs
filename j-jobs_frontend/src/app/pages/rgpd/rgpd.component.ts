import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';

@Component({
    selector: 'app-rgpd',
    imports: [CommonModule, RouterModule],
    templateUrl: './rgpd.component.html',
    styleUrl: './rgpd.component.scss'
})
export class RgpdComponent {
    constructor(private router: Router) { }

    connectUser(): void {
        this.router.navigate(['/login']);
    }

    registerUser(): void {
        this.router.navigate(['/register']);
    }
}
