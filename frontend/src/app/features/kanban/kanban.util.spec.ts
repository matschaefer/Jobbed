import { ApplicationSummary } from '../../core/models/application.model';
import { groupByStatus, needsConfirmation } from './kanban.util';

function app(id: string, status: ApplicationSummary['currentStatus']): ApplicationSummary {
  return {
    id,
    jobTitle: 'Job ' + id,
    company: { id: 'c', name: 'Co' },
    currentStatus: status,
    tags: [],
  };
}

describe('kanban.util', () => {
  it('groups applications into all status columns in order', () => {
    const columns = groupByStatus([app('1', 'APPLIED'), app('2', 'APPLIED'), app('3', 'OFFER')]);

    expect(columns.length).toBe(12);
    expect(columns[0].status).toBe('SAVED');
    const applied = columns.find((c) => c.status === 'APPLIED');
    const offer = columns.find((c) => c.status === 'OFFER');
    expect(applied?.items.map((a) => a.id)).toEqual(['1', '2']);
    expect(offer?.items.length).toBe(1);
    expect(columns.find((c) => c.status === 'SAVED')?.items.length).toBe(0);
  });

  it('flags REJECTED and WITHDRAWN as requiring confirmation', () => {
    expect(needsConfirmation('REJECTED')).toBeTrue();
    expect(needsConfirmation('WITHDRAWN')).toBeTrue();
    expect(needsConfirmation('INTERVIEW')).toBeFalse();
  });
});
