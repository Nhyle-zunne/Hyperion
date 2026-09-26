import { ReviewerTrack } from '../types';

export interface CompetencyInfo {
  code: string; // e.g. "C1"
  functionCode: string | null; // e.g. "F1", "F2", "F3" or null
  functionTitle: string | null;
  title: string;
  examQuota: number;
  reviewer: ReviewerTrack;
}

export const OIC_NW_COMPETENCIES: CompetencyInfo[] = [
  {
    code: 'C1',
    functionCode: 'F1',
    functionTitle: 'F1 – Navigation at the Operational Level',
    title: 'Plan and conduct a passage and determine position',
    examQuota: 35,
    reviewer: 'OIC-NW'
  },
  {
    code: 'C2',
    functionCode: 'F1',
    functionTitle: 'F1 – Navigation at the Operational Level',
    title: 'Maintain a safe navigational watch',
    examQuota: 20,
    reviewer: 'OIC-NW'
  },
  {
    code: 'C3',
    functionCode: 'F1',
    functionTitle: 'F1 – Navigation at the Operational Level',
    title: 'Use of Radar and ARPA to maintain safety of navigation',
    examQuota: 15,
    reviewer: 'OIC-NW'
  },
  {
    code: 'C4',
    functionCode: 'F1',
    functionTitle: 'F1 – Navigation at the Operational Level',
    title: 'Use of ECDIS to maintain safety of navigation',
    examQuota: 10,
    reviewer: 'OIC-NW'
  },
  {
    code: 'C5',
    functionCode: 'F1',
    functionTitle: 'F1 – Navigation at the Operational Level',
    title: 'Respond to emergencies',
    examQuota: 10,
    reviewer: 'OIC-NW'
  },
  {
    code: 'C7',
    functionCode: 'F1',
    functionTitle: 'F1 – Navigation at the Operational Level',
    title: 'Use IMO Standard Marine Communication Phrases (SMCP) and English in written and oral form',
    examQuota: 20,
    reviewer: 'OIC-NW'
  },
  {
    code: 'C9',
    functionCode: 'F1',
    functionTitle: 'F1 – Navigation at the Operational Level',
    title: 'Manoeuvre the ship',
    examQuota: 15,
    reviewer: 'OIC-NW'
  },
  {
    code: 'C10',
    functionCode: 'F2',
    functionTitle: 'F2 – Cargo Handling and Stowage at the Operational Level',
    title: 'Monitor loading, stowage, securing and care during voyage and unloading of cargoes',
    examQuota: 15,
    reviewer: 'OIC-NW'
  },
  {
    code: 'C11',
    functionCode: 'F2',
    functionTitle: 'F2 – Cargo Handling and Stowage at the Operational Level',
    title: 'Inspect and report defect and damage to cargo spaces, hatch covers and ballast tanks',
    examQuota: 15,
    reviewer: 'OIC-NW'
  },
  {
    code: 'C12',
    functionCode: 'F3',
    functionTitle: 'F3 – Controlling the Operation of the Ship and Care for Persons on Board',
    title: 'Ensure compliance with pollution-prevention requirements',
    examQuota: 15,
    reviewer: 'OIC-NW'
  },
  {
    code: 'C13',
    functionCode: 'F3',
    functionTitle: 'F3 – Controlling the Operation of the Ship and Care for Persons on Board',
    title: 'Maintain seaworthiness of the ship',
    examQuota: 15,
    reviewer: 'OIC-NW'
  },
  {
    code: 'C17',
    functionCode: 'F3',
    functionTitle: 'F3 – Controlling the Operation of the Ship and Care for Persons on Board',
    title: 'Monitor compliance with legislative requirements',
    examQuota: 15,
    reviewer: 'OIC-NW'
  }
];

export const GMDSS_COMPETENCIES: CompetencyInfo[] = [
  {
    code: 'C1',
    functionCode: null,
    functionTitle: null,
    title: 'Transmit and receive information using GMDSS subsystems and equipment and fulfilling functional requirements',
    examQuota: 85,
    reviewer: 'GMDSS'
  },
  {
    code: 'C2',
    functionCode: null,
    functionTitle: null,
    title: 'Provide radio services in emergencies',
    examQuota: 15,
    reviewer: 'GMDSS'
  }
];

export function getCompetencyTitle(reviewer: ReviewerTrack, code: string): string {
  const list = reviewer === 'OIC-NW' ? OIC_NW_COMPETENCIES : GMDSS_COMPETENCIES;
  return list.find((it) => it.code.toUpperCase() === code.toUpperCase())?.title ?? `Competency ${code}`;
}

export function getFunctionTitle(code: string): string {
  switch (code.toUpperCase()) {
    case 'F1':
      return 'F1 – Navigation at the Operational Level';
    case 'F2':
      return 'F2 – Cargo Handling and Stowage at the Operational Level';
    case 'F3':
      return 'F3 – Controlling Operation of Ship and Care for Persons';
    default:
      return code;
  }
}
