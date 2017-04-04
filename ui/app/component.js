export function rootComponent(childrenComponents = []) {
  const rootComponent = {
    childrens: childrenComponents,
    elem: document.body
  };

  childrenComponents
    .forEach(children => {
      rootComponent.elem.appendChild(children.elem);
      children.parent = rootComponent;
    });

  return rootComponent;
};

export function buildComponent(definition) {
  return (state, childrenComponents = []) => {
    const componentElement = document.createElement('div');
    componentElement.innerHTML = definition.html(state);
    componentElement.className = definition.class;
    const component = { elem : componentElement, childrens : childrenComponents };

    childrenComponents
      .forEach(children => {
        component.elem.appendChild(children.elem);
        children.parent = component;
      });

    definition.oninit(component, state);

    return component;
  };
};
