import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AbstractKeyPageComponent } from './abstract-key-page.component';

xdescribe('AbstractKeyPageComponent', () => {
  let component: AbstractKeyPageComponent;
  let fixture: ComponentFixture<AbstractKeyPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AbstractKeyPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AbstractKeyPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
