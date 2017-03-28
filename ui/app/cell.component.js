import Rx from 'rxjs/Rx';
import { buildComponent } from './component';

export const cellComponent = buildComponent({
  html: '<input type="text"></input>',
  oninit: (component) => {
    console.log(component);
  }
});
