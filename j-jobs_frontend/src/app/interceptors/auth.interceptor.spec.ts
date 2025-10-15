import { TestBed } from '@angular/core/testing';
import { AuthInterceptor } from './auth.interceptor';
import { OAuth2Service } from '@app/services/oauth2.service';

xdescribe('AuthInterceptor', () => {
  let interceptor: AuthInterceptor;
  let oauth2Service: jasmine.SpyObj<OAuth2Service>;

  beforeEach(() => {
    const oauth2ServiceSpy = jasmine.createSpyObj('OAuth2Service', [
      'getToken',
      'refreshToken',
    ]);

    TestBed.configureTestingModule({
      providers: [
        AuthInterceptor,
        { provide: OAuth2Service, useValue: oauth2ServiceSpy },
      ],
    });

    interceptor = TestBed.inject(AuthInterceptor);
    oauth2Service = TestBed.inject(
      OAuth2Service,
    ) as jasmine.SpyObj<OAuth2Service>;
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });
});