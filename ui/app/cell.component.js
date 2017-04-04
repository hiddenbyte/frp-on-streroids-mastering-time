import Rx from 'rxjs/Rx';
import { buildComponent } from './component';
import { parseCellValue } from './utils';

export const cellComponent = buildComponent({
  class: 'cell',
  html: (state) => `<fieldset>    <legend>${state.id}</legend> <input type="text" id="cell_${state.id}"></input></fieldset>`,
  oninit: (component, state) => {
    component._id = state.id;

    component._input = component.elem.querySelector(`#cell_${state.id}`);

    component.changeBehaviorSubject = new Rx.BehaviorSubject(Object.assign({ cellId : state.id }, parseCellValue('0')));

    component._cellChange = Rx.Observable.fromEvent(component._input, 'blur')
      .map(event => event.target.value)
      .map(value => parseCellValue(value, state.id))
      .filter(value => value.valid)
      .map(value => Object.assign({ cellId : state.id }, value))
      .multicast(component.changeBehaviorSubject)
      .refCount();

    component._setValue = function(value) {
      component._input.value = value;
      component.changeBehaviorSubject.next(Object.assign({ cellId : state.id, computed: true  }, parseCellValue(value)));
      component._input.classList.add('highlighted');
      setTimeout(() => component._input.classList.remove('highlighted'), 100);
    };

    component._setDepedentCellSub = function(subscription) {
      component.depedentCellSub = subscription;
    };
    component._clearDepedentCellSub = function(subscription) {
      if (component.depedentCellSub) {
        component.depedentCellSub.unsubscribe();
      }
    };
  }
});
