import { buildComponent, rootComponent } from './component';
import { cellComponent } from './cell.component';

const mainComponent = buildComponent({
  html : '',
  oninit: (component) => {
    console.log('main');
    console.log(component);
  }
});

rootComponent([
  mainComponent([
    cellComponent(),
    cellComponent(),
    cellComponent(),
    cellComponent(),
    cellComponent(),
    cellComponent(),
    cellComponent()
  ])
]);
