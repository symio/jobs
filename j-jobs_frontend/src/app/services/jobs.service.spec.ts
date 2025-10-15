import { TestBed } from '@angular/core/testing';
import {
    HttpClientTestingModule,
    HttpTestingController,
} from '@angular/common/http/testing';
import {
    JobsService,
    Job,
    GetJobsResponse,
    CreateJobRequest,
    UpdateJobRequest,
    SearchJobsRequest,
} from './jobs.service';
import { ApiurlService } from '@app/environments/apiurl.service';
import { OAuth2Service } from './oauth2.service';
import { HttpHeaders } from '@angular/common/http';

describe('JobsService', () => {
    let service: JobsService;
    let httpMock: HttpTestingController;
    let apiurlService: jasmine.SpyObj<ApiurlService>;
    let oAuth2Service: jasmine.SpyObj<OAuth2Service>;

    const mockApiBaseUrl = 'http://localhost:8080/api';
    const mockHeaders = new HttpHeaders({
        'Content-Type': 'application/hal+json',
        Authorization: 'Bearer mock-token',
    });

    const mockJob: Job = {
        position: 'Développeur Fullstack Java / Spring / Angular',
        description: 'Description du poste',
        compagny: 'ACME',
        city: 'Niort',
        from_official_dom: false,
        created_at: '2025-01-01T00:00:00Z',
        updated_at: '2025-01-01T00:00:00Z',
        job_has_statuses: [
            {
                job_status: 'active',
                offer_status: 'pending',
                applied_at: '2025-01-01T00:00:00Z',
            },
        ],
        contract: 'CDI',
        workTime: 'full-time',
        workMode: 'remote',
        offerStatus: 'pending',
        _links: {
            self: { href: '/jobs/1' },
        },
    };

    const mockGetJobsResponse: GetJobsResponse = {
        _embedded: {
            jobs: [mockJob],
        },
        _links: {
            self: { href: '/jobs?page=0&size=3' },
        },
        page: {
            size: 3,
            total_elements: 1,
            total_pages: 1,
            number: 0,
        },
    };

    beforeEach(() => {
        const apiurlServiceSpy = jasmine.createSpyObj('ApiurlService', [
            'getApiBaseUrl',
        ]);
        const oAuth2ServiceSpy = jasmine.createSpyObj('OAuth2Service', [
            'buildRequestHeadersHal',
            'buildRequestHeaders',
        ]);

        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                JobsService,
                { provide: ApiurlService, useValue: apiurlServiceSpy },
                { provide: OAuth2Service, useValue: oAuth2ServiceSpy },
            ],
        }).compileComponents();

        apiurlService = TestBed.inject(
            ApiurlService,
        ) as jasmine.SpyObj<ApiurlService>;
        oAuth2Service = TestBed.inject(
            OAuth2Service,
        ) as jasmine.SpyObj<OAuth2Service>;
        
        apiurlService.getApiBaseUrl.and.returnValue(mockApiBaseUrl);
        oAuth2Service.buildRequestHeadersHal.and.returnValue(mockHeaders);
        oAuth2Service.buildRequestHeaders.and.returnValue(mockHeaders);
        
        service = TestBed.inject(JobsService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('createJob', () => {
        it('should create a job via POST request', () => {
            const newJob: CreateJobRequest = {
                position: 'Développeur Fullstack Java / Spring / Angular',
                compagny: 'ACME',
                city: 'Niort',
                contract: 'CDI',
                workTime: 'full-time',
                workMode: 'remote',
            };

            service.createJob(newJob).subscribe((job) => {
                expect(job).toEqual(mockJob);
            });

            const req = httpMock.expectOne(`${mockApiBaseUrl}/jobs`);
            expect(req.request.method).toBe('POST');
            expect(req.request.body).toEqual(newJob);
            expect(req.request.headers.get('Content-Type')).toBe(
                'application/hal+json',
            );
            req.flush(mockJob);
        });

        it('should call buildRequestHeadersHal with true', () => {
            const newJob: CreateJobRequest = {
                position: 'Dev',
                compagny: 'ACME',
                city: 'Niiort',
            };

            service.createJob(newJob).subscribe();

            const req = httpMock.expectOne(`${mockApiBaseUrl}/jobs`);
            req.flush(mockJob);

            expect(oAuth2Service.buildRequestHeadersHal).toHaveBeenCalledWith(true);
        });
    });

    describe('updateJob', () => {
        it('should update a job via PATCH request', () => {
            const updateData: UpdateJobRequest = {
                position: 'Senior Développeur Fullstack Java / Spring / Angular',
                contract: 'CDI',
            };
            const jobLink = '/jobs/1';

            service.updateJob(jobLink, updateData).subscribe((job) => {
                expect(job).toEqual(mockJob);
            });

            const req = httpMock.expectOne(`${mockApiBaseUrl}/jobs/1`);
            expect(req.request.method).toBe('PATCH');
            expect(req.request.body).toEqual(updateData);
            req.flush(mockJob);
        });

        it('should normalize HAL link with double prefix before making request', () => {
            const updateData: UpdateJobRequest = { position: 'Updated Position' };
            const jobLink = '/jobs/jobs/1'; // Lien avec double /jobs

            service.updateJob(jobLink, updateData).subscribe();

            const req = httpMock.expectOne(`${mockApiBaseUrl}/jobs/1`);
            expect(req.request.method).toBe('PATCH');
            req.flush(mockJob);
        });
    });

    describe('searchJobs', () => {
        it('should search jobs with criteria', () => {
            const searchCriteria: SearchJobsRequest = {
                contract: 'CDI',
                eventBefore: null,
                eventAfter: null,
                officialdom: false,
                offerStatus: 'pending',
                sort: 'created_at',
                textual: 'Angular',
                workTime: 'full-time',
                workMode: 'remote',
            };

            service.searchJobs(searchCriteria, 0, 3).subscribe((response) => {
                expect(response).toEqual(mockGetJobsResponse);
                expect(response._embedded.jobs.length).toBe(1);
            });

            const req = httpMock.expectOne(
                `${mockApiBaseUrl}/jobs/search?page=0&size=3`,
            );
            expect(req.request.method).toBe('POST');
            expect(req.request.body).toEqual(searchCriteria);
            req.flush(mockGetJobsResponse);
        });

        it('should use default pagination values', () => {
            const searchCriteria: SearchJobsRequest = {
                contract: null,
                eventBefore: null,
                eventAfter: null,
                officialdom: false,
                offerStatus: null,
                sort: 'created_at',
                textual: null,
                workTime: null,
                workMode: null,
            };

            service.searchJobs(searchCriteria).subscribe();

            const req = httpMock.expectOne(
                `${mockApiBaseUrl}/jobs/search?page=0&size=3`,
            );
            expect(req.request.method).toBe('POST');
            req.flush(mockGetJobsResponse);
        });
    });

    describe('getJobs', () => {
        it('should get jobs with pagination', () => {
            service.getJobs(0, 3).subscribe((response) => {
                expect(response).toEqual(mockGetJobsResponse);
            });

            const req = httpMock.expectOne(`${mockApiBaseUrl}/jobs?page=0&size=3`);
            expect(req.request.method).toBe('GET');
            req.flush(mockGetJobsResponse);
        });

        it('should use default pagination values', () => {
            service.getJobs().subscribe();

            const req = httpMock.expectOne(`${mockApiBaseUrl}/jobs?page=0&size=3`);
            expect(req.request.method).toBe('GET');
            req.flush(mockGetJobsResponse);
        });
    });

    describe('getJobByHalLink', () => {
        it('should get a job by HAL link', () => {
            const jobLink = '/jobs/1';

            service.getJobByHalLink(jobLink).subscribe((job) => {
                expect(job).toEqual(mockJob);
            });

            const req = httpMock.expectOne(`${mockApiBaseUrl}/jobs/1`);
            expect(req.request.method).toBe('GET');
            req.flush(mockJob);
        });

        it('should normalize HAL link', () => {
            const jobLink = '/jobs/jobs/1';

            service.getJobByHalLink(jobLink).subscribe();

            const req = httpMock.expectOne(`${mockApiBaseUrl}/jobs/1`);
            req.flush(mockJob);
        });
    });

    describe('deleteJob', () => {
        it('should delete a job and notify subscribers', (done) => {
            const jobLink = '/jobs/1';
            let notificationReceived = false;

            service.jobsUpdated$.subscribe((updated) => {
                if (updated) {
                    notificationReceived = true;
                }
            });

            service.deleteJob(jobLink).subscribe(() => {
                expect(notificationReceived).toBe(true);
                done();
            });

            const req = httpMock.expectOne(`${mockApiBaseUrl}/jobs/1`);
            expect(req.request.method).toBe('DELETE');
            req.flush(null);
        });

        it('should use buildRequestHeaders instead of buildRequestHeadersHal', () => {
            const jobLink = '/jobs/1';

            service.deleteJob(jobLink).subscribe();

            const req = httpMock.expectOne(`${mockApiBaseUrl}/jobs/1`);
            req.flush(null);

            expect(oAuth2Service.buildRequestHeaders).toHaveBeenCalledWith(true);
        });
    });

    describe('notifyJobsUpdated', () => {
        it('should emit notification when called', (done) => {
            service.jobsUpdated$.subscribe((updated) => {
                if (updated) {
                    expect(updated).toBe(true);
                    done();
                }
            });

            service.notifyJobsUpdated();
        });
    });

    describe('normalizeHalLink (private method behavior)', () => {
        it('should handle links starting with /jobs', () => {
            service.getJobByHalLink('/jobs/123').subscribe();

            const req = httpMock.expectOne(`${mockApiBaseUrl}/jobs/123`);
            req.flush(mockJob);
        });

        it('should handle links not starting with /jobs', () => {
            service.getJobByHalLink('/123').subscribe();

            const req = httpMock.expectOne(`${mockApiBaseUrl}/123`);
            req.flush(mockJob);
        });
    });

    describe('error handling', () => {
        it('should handle HTTP errors on createJob', () => {
            const newJob: CreateJobRequest = {
                position: 'Dev',
                compagny: 'ACME',
                city: 'Niort',
            };

            service.createJob(newJob).subscribe({
                next: () => fail('Should have failed'),
                error: (error) => {
                    expect(error.status).toBe(400);
                },
            });

            const req = httpMock.expectOne(`${mockApiBaseUrl}/jobs`);
            req.flush('Bad Request', { status: 400, statusText: 'Bad Request' });
        });

        it('should handle HTTP errors on getJobs', () => {
            service.getJobs().subscribe({
                next: () => fail('Should have failed'),
                error: (error) => {
                    expect(error.status).toBe(500);
                },
            });

            const req = httpMock.expectOne(`${mockApiBaseUrl}/jobs?page=0&size=3`);
            req.flush('Server Error', {
                status: 500,
                statusText: 'Internal Server Error',
            });
        });
    });
});
