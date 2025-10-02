// src/app/services/page-title.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class PageTitleService {
    private pageTitleSubject = new BehaviorSubject<string>('');
    public pageTitle$: Observable<string> = this.pageTitleSubject.asObservable();

    setTitle(title: string): void {
        this.pageTitleSubject.next(title);
    }

    getTitle(): string {
        return this.pageTitleSubject.value;
    }
}
