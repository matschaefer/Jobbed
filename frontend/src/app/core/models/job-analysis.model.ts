export interface JobAnalysisRequest {
  jobDescription: string;
  profileSkills: string[];
}

export interface JobAnalysisResult {
  detectedSkills: string[];
  programmingLanguages: string[];
  frameworksAndTools: string[];
  spokenLanguages: string[];
  benefits: string[];
  matchedSkills: string[];
  missingSkills: string[];
  matchPercentage: number;
  suggestions: string[];
  seniorityLevel: 'JUNIOR' | 'SENIOR' | 'LEAD' | 'NOT_SPECIFIED';
  workModel: 'REMOTE' | 'HYBRID' | 'ON_SITE' | 'NOT_SPECIFIED';
  salaryHints: string[];
  keywords: string[];
  analyzer: string;
}

export interface AiStatus {
  available: boolean;
  provider: string;
  model: string;
}
