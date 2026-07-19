/**
 * Produktions-Environment. Im Container läuft die API hinter demselben Origin
 * (nginx-Reverse-Proxy), daher relativer Basis-Pfad.
 */
export const environment = {
  production: true,
  apiBaseUrl: '/api/v1',
};
