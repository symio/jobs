import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeactivateStep2Component } from './deactivate-step2.component';

xdescribe('DeactivateStep2Component', () => {
  let component: DeactivateStep2Component;
  let fixture: ComponentFixture<DeactivateStep2Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeactivateStep2Component]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DeactivateStep2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
