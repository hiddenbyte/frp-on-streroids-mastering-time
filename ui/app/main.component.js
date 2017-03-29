import Rx from 'rxjs/Rx';
import { buildComponent, rootComponent } from './component';
import { cellComponent } from './cell.component';
import { requestComputation } from './http';

function loadParameter(parameter, cellChanges) {
  if (isNaN(parameter)) {
    return cellChanges
      .filter(change => change.cellId === parameter && change.type === 'literal')
      .map(change => change.value)
      .take(1);
  } else {
    return Rx.Observable.of(+parameter);
  }
}

function loadParameters(parameters, cellChanges) {
  return parameters.map(param => loadParameter(param, cellChanges))
    .reduce((acc,val) => Rx.Observable.merge(acc, val), Rx.Observable.empty())
    .toArray()
}

function compute(change, cellChanges) {
  if (change.type === 'literal') {
    return Rx.Observable.of({
      cellId : change.cellId,
      value: change.value,
      dependentCells: [],
      change: change
    });
  }

  if (change.type === 'computation') {
    return loadParameters(change.parameters, cellChanges)
      .mergeMap(parameters => requestComputation(change.function, parameters))
      .map(computation => computation.result)
      .map(result => ({
        cellId : change.cellId,
        value: result,
        dependentCells: change.parameters.filter(param => isNaN(param)),
        change: change
      }));
  }
}

function createDependentCellObservableSub(change, changes, cells) {
  const dependentObservable = change.parameters
    .filter(param => isNaN(param))
    .map(param => changes.filter(change => change.cellId === param))
    .reduce((acc, val) => Rx.Observable.merge(acc, val), Rx.Observable.empty())
    .map(() => change);

  const dependentObservableSub = dependentObservable
    .mergeMap(change => compute(change, changes))
    .subscribe(computed => {
      const cell = cells.find(cell => cell._id === computed.cellId);
      cell._setValue(computed.value);
    });

  return dependentObservableSub;
}

const mainComponent = buildComponent({
  class : 'sheet',
  html : (state) => '',
  oninit: (component, state) => {
    const cells = component.childrens;

    const changes = cells.map(cell => cell._cellChange)
      .reduce((acc, val) => Rx.Observable.merge(acc, val), Rx.Observable.empty());

    changes
      .filter(change => !change.computed)
      .mergeMap(change => compute(change, changes))
      .subscribe(computed => {
          console.log(computed);
          const cell = cells.find(cell => cell._id === computed.cellId);
          cell._setValue(computed.value);
          cell._clearDepedentCellSub(computed.changeRequest, changes, cells);

          if (computed.dependentCells.length > 0) {
            cell._setDepedentCellSub(createDependentCellObservableSub(computed.change, changes, cells));
          }
      });
  }
});

rootComponent([
  mainComponent({}, [
    cellComponent({ id: 'A1', store: store }),
    cellComponent({ id: 'A2', store: store }),
    cellComponent({ id: 'A3', store: store }),
    cellComponent({ id: 'A4', store: store }),
    cellComponent({ id: 'B1', store: store }),
    cellComponent({ id: 'B2', store: store }),
    cellComponent({ id: 'B3', store: store}),
    cellComponent({ id: 'B4', store: store })
  ])
]);
