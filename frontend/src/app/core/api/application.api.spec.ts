import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationApi } from './application.api';

describe('ApplicationApi', () => {
  let api: ApplicationApi;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), ApplicationApi],
    });
    api = TestBed.inject(ApplicationApi);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('builds list query params including repeated status filters', () => {
    api.list({ page: 0, size: 20, query: 'java', status: ['APPLIED', 'INTERVIEW'] }).subscribe();

    const req = httpMock.expectOne((r) => r.url === '/api/v1/applications');
    expect(req.request.params.get('query')).toBe('java');
    expect(req.request.params.getAll('status')).toEqual(['APPLIED', 'INTERVIEW']);
    req.flush({ content: [], totalElements: 0 });
  });

  it('sends status change with note', () => {
    api.changeStatus('a1', 'REJECTED', 'kein Match').subscribe();

    const req = httpMock.expectOne('/api/v1/applications/a1/status');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ newStatus: 'REJECTED', note: 'kein Match' });
    req.flush({});
  });
});
