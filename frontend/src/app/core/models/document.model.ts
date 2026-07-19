export type DocumentType = 'CV' | 'COVER_LETTER' | 'CERTIFICATE' | 'REFERENCE' | 'PORTFOLIO' | 'JOB_DESCRIPTION' | 'OTHER';

export interface ApplicationDocument {
  id: string; applicationId?: string; documentType: DocumentType; originalFileName: string;
  mimeType: string; fileSize: number; description?: string; createdAt: string;
}
