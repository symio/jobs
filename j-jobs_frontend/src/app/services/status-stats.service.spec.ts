import { TestBed } from '@angular/core/testing';

import { StatusStatsService } from './status-stats.service';

xdescribe('StatusStatsService', () => {
  let service: StatusStatsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StatusStatsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
