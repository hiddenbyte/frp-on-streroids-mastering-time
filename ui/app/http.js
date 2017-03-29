import Rx from 'rxjs/Rx';

export function requestComputation(fn, parameters) {
  const requestComputationPromise = fetch('http://localhost:8080', {
    method: 'POST',
    body: JSON.stringify({
      function: fn,
      parameters: parameters.map(n => (+n).toFixed(1))
    })
  });

  return Rx.Observable.fromPromise(requestComputationPromise)
    .mergeMap(response => Rx.Observable.fromPromise(response.json()));
}
