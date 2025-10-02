import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MenuService, MenuData, MenuDataNoTitle } from '../../services/menu.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
    selector: 'app-mobile-menu',
    imports: [CommonModule],
    templateUrl: './mobile-menu.component.html',
    styleUrl: './mobile-menu.component.scss',
    standalone: true
})
export class MobileMenuComponent implements OnInit {
    @Input() isMenuOpen: boolean = false;
    @Output() menuClosed = new EventEmitter<void>();
    @Input() isAuthenticated: boolean = false;

    menuData: MenuData = { title: '', items: [] };
    sideMenuData: MenuDataNoTitle = { items: [] };

    constructor(
        private menuService: MenuService,
        private sanitizer: DomSanitizer
    ) { }

    getIcon(name: string): SafeHtml {
        const iconHtml = this.menuService.getIcon(name);
        return this.sanitizer.bypassSecurityTrustHtml(iconHtml);
    }

    ngOnInit(): void {
        this.menuData = this.menuService.getMenuData();
        this.sideMenuData = this.menuService.getSideMenuData();
    }

    closeMenu(): void {
        this.menuClosed.emit();
    }
}
