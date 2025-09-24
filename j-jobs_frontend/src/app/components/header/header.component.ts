import { Component, EventEmitter, Output } from '@angular/core';

@Component({
    selector: 'app-header',
    imports: [],
    templateUrl: './header.component.html',
    styleUrl: './header.component.scss',
    standalone: true
})
export class HeaderComponent {
    
    @Output() menuToggled = new EventEmitter<void>();

    toggleMenu(): void {
        this.menuToggled.emit();
    }

}
