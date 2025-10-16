import { Injectable } from '@angular/core';

export interface MenuItemIcon {
  label: string;
  link: string;
  icon: string;
}
export interface MenuItem {
  label: string;
  link: string;
}

export interface MenuDataNoTitle {
  items: MenuItemIcon[];
}
export interface MenuData {
  title: string;
  items: MenuItem[];
}

@Injectable({
  providedIn: 'root',
})
export class MenuService {
  icons: { [key: string]: string } = {
    statistiques:
      '<svg class="iconeDesktop histo" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" data-name="Layer 1"><path d="M23,22H5a3,3,0,0,1-3-3V1A1,1,0,0,0,0,1V19a5.006,5.006,0,0,0,5,5H23a1,1,0,0,0,0-2Z"></path><path d="M6,20a1,1,0,0,0,1-1V12a1,1,0,0,0-2,0v7A1,1,0,0,0,6,20Z"></path><path d="M10,10v9a1,1,0,0,0,2,0V10a1,1,0,0,0-2,0Z"></path><path d="M15,13v6a1,1,0,0,0,2,0V13a1,1,0,0,0-2,0Z"></path><path d="M20,9V19a1,1,0,0,0,2,0V9a1,1,0,0,0-2,0Z"></path><path d="M6,9a1,1,0,0,0,.707-.293l3.586-3.586a1.025,1.025,0,0,1,1.414,0l2.172,2.172a3,3,0,0,0,4.242,0l5.586-5.586A1,1,0,0,0,22.293.293L16.707,5.878a1,1,0,0,1-1.414,0L13.121,3.707a3,3,0,0,0-4.242,0L5.293,7.293A1,1,0,0,0,6,9Z"></path></svg>',
    ajoutemploi:
      '<svg class="iconeDesktop ajoutEmploi" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" data-name="Layer 1"><path d="m14 7v-6.54a6.977 6.977 0 0 1 2.465 1.59l3.484 3.486a6.954 6.954 0 0 1 1.591 2.464h-6.54a1 1 0 0 1 -1-1zm8 3.485v8.515a5.006 5.006 0 0 1 -5 5h-10a5.006 5.006 0 0 1 -5-5v-14a5.006 5.006 0 0 1 5-5h4.515c.163 0 .324.013.485.024v6.976a3 3 0 0 0 3 3h6.976c.011.161.024.322.024.485zm-6 6.515a1 1 0 0 0 -1-1h-2v-2a1 1 0 0 0 -2 0v2h-2a1 1 0 0 0 0 2h2v2a1 1 0 0 0 2 0v-2h2a1 1 0 0 0 1-1z"></path></svg>',
    dashboard:
      '<svg class="iconeDesktop house" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" fill="currentColor"><g><path d="M256,319.841c-35.346,0-64,28.654-64,64v128h128v-128C320,348.495,291.346,319.841,256,319.841z"></path><g><path d="M362.667,383.841v128H448c35.346,0,64-28.654,64-64V253.26c0.005-11.083-4.302-21.733-12.011-29.696l-181.29-195.99c-31.988-34.61-85.976-36.735-120.586-4.747c-1.644,1.52-3.228,3.103-4.747,4.747L12.395,223.5C4.453,231.496-0.003,242.31,0,253.58v194.261c0,35.346,28.654,64,64,64h85.333v-128c0.399-58.172,47.366-105.676,104.073-107.044C312.01,275.383,362.22,323.696,362.667,383.841z"></path><path d="M256,319.841c-35.346,0-64,28.654-64,64v128h128v-128C320,348.495,291.346,319.841,256,319.841z"></path></g></g></svg>',
  };

  getIcon(name: string): string {
    return this.icons[name];
  }

  getSideMenuData(): MenuDataNoTitle {
    return {
      items: [
        { label: 'Statistiques', link: '#', icon: 'statistiques' },
        {
          label: 'Ajouter un emploi',
          link: '/dashboard/job-form',
          icon: 'ajoutemploi',
        },
        { label: 'Tableau de bord', link: '/dashboard', icon: 'dashboard' },
      ],
    };
  }
  getMenuData(): MenuData {
    return {
      title: 'Nous connaître',
      items: [
        { label: 'Mentions légales', link: 'mentions-legales' },
        { label: 'Politique de confidentialité', link: 'confidentialite' },
        { label: 'Aide / Nous contacter', link: 'aide' },
      ],
    };
  }
}
