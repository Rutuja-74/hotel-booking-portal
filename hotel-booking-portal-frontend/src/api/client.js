import axios from 'axios';

// One error type for the whole app. Pages only need error.message.
export class ApiError extends Error {
  constructor(message, status = 0, fieldErrors = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

const client = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
});

// Turn every failure into an ApiError with a friendly message.
client.interceptors.response.use(
  (response) => response,
  (error) => {
    // A cancelled request is not a real error, so pass it through unchanged.
    if (axios.isCancel(error)) {
      return Promise.reject(error);
    }

    if (error.code === 'ECONNABORTED') {
      return Promise.reject(new ApiError('The server took too long to respond. Please try again.'));
    }

    if (!error.response) {
      return Promise.reject(
        new ApiError('Cannot reach the server. Please make sure the backend is running and try again.')
      );
    }

    const { status, data } = error.response;
    const fieldErrors = data && data.fieldErrors ? data.fieldErrors : null;

    let message = data && data.message ? data.message : '';
    if (fieldErrors) {
      message = Object.values(fieldErrors).join(' ');
    }
    if (!message) {
      message =
        status >= 500
          ? 'The server is not responding right now. Please make sure the backend is running and try again.'
          : `Something went wrong (error ${status}). Please try again.`;
    }

    return Promise.reject(new ApiError(message, status, fieldErrors));
  }
);

export const isCancel = axios.isCancel;
export default client;