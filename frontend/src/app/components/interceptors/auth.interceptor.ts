import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Token aus dem localStorage des Browsers holen
  const token = localStorage.getItem('token');

  // Ausnahme für Giphy API (kein Auth-Header)
  if (req.url.includes ('giphy.com')) {
    return next(req);
  }


  // Prüfen: Haben wir einen Token?
  if (token) {
    // Wenn JA: Wir müssen den Request klonen (Requests sind immutable)
    // und den Authorization-Header hinzufügen.
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

    // Den modifizierten Request (mit Token) weiterschicken
    return next(clonedRequest);
  }

  // Wenn kein Token da ist, schicken wir den Original-Request
  return next(req);
};
