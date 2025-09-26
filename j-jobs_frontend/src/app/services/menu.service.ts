import { Injectable } from '@angular/core';

export interface MenuItem {
  label: string;
  link: string;
}

export interface MenuData {
  title: string;
  items: MenuItem[];
}

@Injectable({
  providedIn: 'root'
})
export class MenuService {

  getMenuData(): MenuData {
    return {
      title: 'Nous connaître',
      items: [
        { label: 'Mentions légales', link: '#' },
        { label: 'Politique de confidentialité', link: '#' },
        { label: 'Aide / Nous contacter', link: '#' }
      ]
    };
  }
}
