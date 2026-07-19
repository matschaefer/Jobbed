export interface ResumeGenerationRequest {
  fullName: string;
  email: string;
  phone?: string;
  location?: string;
  headline?: string;
  professionalSummary?: string;
  skills?: string;
  experience?: string;
  education?: string;
  languages?: string;
  targetRole?: string;
  jobDescription?: string;
}

export interface ResumeExperience {
  role: string;
  company: string;
  period: string;
  bullets: string[];
}

export interface ResumeResult {
  fullName: string;
  headline: string;
  contactLine: string;
  professionalSummary: string;
  coreSkills: string[];
  experience: ResumeExperience[];
  education: string[];
  languages: string[];
  highlights: string[];
  generatedBy: string;
}
