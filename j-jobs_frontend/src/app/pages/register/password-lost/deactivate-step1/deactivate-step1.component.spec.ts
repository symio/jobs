import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeactivateStep1Component } from './deactivate-step1.component';

xdescribe('DeactivateStep1Component', () => {
  let component: DeactivateStep1Component;
  let fixture: ComponentFixture<DeactivateStep1Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeactivateStep1Component]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DeactivateStep1Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
