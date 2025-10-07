import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  MenuService,
  MenuData,
  MenuDataNoTitle,
} from '../../services/menu.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
  selector: 'app-footer',
  imports: [CommonModule],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss',
  standalone: true,
})
export class FooterComponent implements OnInit {
  @Input() showNavMenu: boolean = false;
  @Input() isAuthenticated: boolean = false;

  menuData: MenuData = { title: '', items: [] };

  constructor(private menuService: MenuService) {}

  ngOnInit(): void {
    this.menuData = this.menuService.getMenuData();
  }
}
