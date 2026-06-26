import { http, unwrap } from './http';
import type { PageResult, PageQuery } from '@/types/api';
import type {
  AiMessage,
  AiSession,
  Favorite,
  FavoritePayload,
  FavoriteQuery,
  Feedback,
  FeedbackPayload,
  FeedbackQuery,
  InboxQuery,
  NotificationInbox,
  SavedSearch,
  SavedSearchPayload,
  SubscriptionMatch,
  SubscriptionMatchQuery,
  SubscriptionRule,
  SubscriptionRulePayload,
  SubscriptionRuleQuery
} from '@/types/workspace';

export function getInbox(params: InboxQuery = { pageNo: 1, pageSize: 5 }) {
  return unwrap<PageResult<NotificationInbox>>(http.get('/system/notifications/inbox', { params }));
}

export function getSubscriptionMatches(params: SubscriptionMatchQuery = { pageNo: 1, pageSize: 5 }) {
  return unwrap<PageResult<SubscriptionMatch>>(http.get('/subscription/matches', { params }));
}

export function markAllSubscriptionMatchesRead() {
  return unwrap<number>(http.put('/subscription/matches/read-all'));
}

export function getSubscriptionMatchUnreadCount() {
  return unwrap<number>(http.get('/subscription/matches/unread-count'));
}

export function getAiSessions(params: PageQuery = { pageNo: 1, pageSize: 20 }) {
  return unwrap<PageResult<AiSession>>(http.get('/ai/sessions', { params }));
}

export function getAiMessages(sessionId: number | string, params: PageQuery = { pageNo: 1, pageSize: 50 }) {
  return unwrap<PageResult<AiMessage>>(http.get('/ai/messages', { params: { ...params, sessionId } }));
}

export function deleteAiSession(id: number | string) {
  return unwrap<void>(http.delete(`/ai/sessions/${id}`));
}

export function getSavedSearches(params: PageQuery = { pageNo: 1, pageSize: 10 }) {
  return unwrap<PageResult<SavedSearch>>(http.get('/search/saved', { params }));
}

export function createSavedSearch(payload: SavedSearchPayload) {
  return unwrap<number>(http.post('/search/saved', payload));
}

export function deleteSavedSearch(id: number | string) {
  return unwrap<void>(http.delete(`/search/saved/${id}`));
}

export function getSubscriptionRules(params: SubscriptionRuleQuery = { pageNo: 1, pageSize: 10 }) {
  return unwrap<PageResult<SubscriptionRule>>(http.get('/subscription/rules', { params }));
}

export function createSubscriptionRule(payload: SubscriptionRulePayload) {
  return unwrap<number>(http.post('/subscription/rules', payload));
}

export function updateSubscriptionRule(id: number | string, payload: SubscriptionRulePayload) {
  return unwrap<void>(http.put(`/subscription/rules/${id}`, payload));
}

export function deleteSubscriptionRule(id: number | string) {
  return unwrap<void>(http.delete(`/subscription/rules/${id}`));
}

export function markSubscriptionMatchRead(id: number | string) {
  return unwrap<void>(http.put(`/subscription/matches/${id}/read`));
}

export function getFavorites(params: FavoriteQuery = { pageNo: 1, pageSize: 10 }) {
  return unwrap<PageResult<Favorite>>(http.get('/workspace/favorites', { params }));
}

export function createFavorite(payload: FavoritePayload) {
  return unwrap<number>(http.post('/workspace/favorites', payload));
}

export function deleteFavorite(id: number | string) {
  return unwrap<void>(http.delete(`/workspace/favorites/${id}`));
}

export function getFeedbacks(params: FeedbackQuery = { pageNo: 1, pageSize: 10 }) {
  return unwrap<PageResult<Feedback>>(http.get('/workspace/feedbacks', { params }));
}

export function createFeedback(payload: FeedbackPayload) {
  return unwrap<number>(http.post('/workspace/feedbacks', payload));
}

export function deleteFeedback(id: number | string) {
  return unwrap<void>(http.delete(`/workspace/feedbacks/${id}`));
}

export function markNotificationRead(id: number | string) {
  return unwrap<void>(http.put(`/system/notifications/${id}/read`));
}

export function markAllNotificationsRead() {
  return unwrap<number>(http.put('/system/notifications/inbox/read-all'));
}

export function getNotificationUnreadCount() {
  return unwrap<number>(http.get('/system/notifications/inbox/unread-count'));
}
