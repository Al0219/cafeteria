import { CanMatchFn, Route, UrlSegment, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export function roleGuard(allowed: string[]): CanMatchFn {
  return (_route: Route, _segments: UrlSegment[]) => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const ok = auth.hasAny(allowed);
    if (!ok) {
      router.navigate(['/']);
    }
    return ok;
  };
}

