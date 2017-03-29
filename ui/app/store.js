import Rx from 'rxjs/Rx';

export class Store {

  constructor() {
    this.CellChanged = new Rx.BehaviorSubject({ cellId: 'XX' });
  }

  getCellChanged() {
    return getCellChanged;
  }
}
