import ky, { HTTPError, type BeforeErrorHook } from "ky";
import type { ApiErrorBody } from "./types";

/** HTTPError augmented with the parsed backend ErrorResponse body. */
export interface ApiHttpError extends Error {
  apiBody?: ApiErrorBody;
}

/**
 * Ky v2 beforeError hook. `state` carries `error: Error`, `request`, `options`,
 * and `retryCount`. When the error is an HTTPError we fire an async background
 * parse of the backend ErrorResponse and mutate-message the Error synchronously
 * returned value before any UI reads it.
 */
const enrichError: BeforeErrorHook = (state) => {
  const { error } = state;
  if (error instanceof HTTPError) {
    void error.response
      .clone()
      .json()
      .then((body: unknown) => {
        const api = body as ApiErrorBody;
        if (api && typeof api?.message === "string") {
          (error as ApiHttpError).apiBody = api;
          error.message = `${api.status} ${api.error}: ${api.message}`;
        }
      })
      .catch(() => {
        /* leave the original message as-is */
      });
  }
  return error;
};

export const http = ky.create({
  prefix: "/api/v1",
  timeout: 60_000,
  retry: 0,
  hooks: { beforeError: [enrichError] },
});
