/**
 * Variables needed for Lueckentext
 */
export interface LueckentextGame {
  textWithBlank: string;
  translation: string;
  gameId?: string;
  dummyWords: string[];
  answer: string;
  synonyms: string[];
}
export interface LueckentextGameListResponse {
  tasks: any;
}
