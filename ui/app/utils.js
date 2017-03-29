function parseAsNumber(value) {
  if (isNaN(value)) {
    return { valid: false };
  }
  return { valid: true, type: 'literal', value: +value };
}

function parseAsExpression(value) {
  const expression = value.split(' ');

  if (expression.length < 3) {
    return { valid: false }
  }

  const fn = expression[0];
  const parameters = expression.slice(1);

  return {
    valid: true,
    type: 'computation',
    function: fn,
    parameters: parameters
  };
}

export function parseCellValue(value) {
  const number = parseAsNumber(value);

  if (number.valid) {
    return number;
  }

  return parseAsExpression(value);
}
