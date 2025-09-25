import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MenuService, MenuData } from '../../services/menu.service';

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

  menuData: MenuData = { title: '', items: [] };

  constructor(private menuService: MenuService) {}

  ngOnInit(): void {
    this.menuData = this.menuService.getMenuData();
  }

  closeMenu(): void {
    this.menuClosed.emit();
  }
}
