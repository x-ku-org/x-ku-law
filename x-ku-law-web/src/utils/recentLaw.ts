const LAST_LAW_DOCUMENT_ID_KEY = 'xku:last-law-document-id';

export function getLastLawDocumentId() {
  return window.localStorage.getItem(LAST_LAW_DOCUMENT_ID_KEY);
}

export function setLastLawDocumentId(documentId: string | number) {
  window.localStorage.setItem(LAST_LAW_DOCUMENT_ID_KEY, String(documentId));
}
