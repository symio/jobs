import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-aide',
    imports: [CommonModule, RouterModule],
  templateUrl: './aide.component.html',
  styleUrl: './aide.component.scss'
})
export class AideComponent {
    constructor(private router: Router) { }

    connectUser(): void {
        this.router.navigate(['/login']);
    }

    registerUser(): void {
        this.router.navigate(['/register']);
    }
}
