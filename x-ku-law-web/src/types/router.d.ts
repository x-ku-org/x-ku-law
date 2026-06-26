import 'vue-router';

declare module 'vue-router' {
  interface RouteMeta {
    skeletonLayout?: 'generic' | 'home' | 'search' | 'law' | 'table' | 'feed' | 'compare' | 'ai';
  }
}
