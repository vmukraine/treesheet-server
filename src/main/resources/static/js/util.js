const ctxUtil = {
  drawTextWithBackground: function(ctx, text, x, y, font, padding) {
    
    // 1. Save the current canvas state (colors, fonts, etc.)
    ctx.save();
    
    // Apply the desired font for accurate measurement
    ctx.font = font || '24px Arial'; 
    
    // Measure the text dimensions
    const metrics = ctx.measureText(text);
    const textWidth = metrics.width;
    const textHeight = metrics.actualBoundingBoxAscent + metrics.actualBoundingBoxDescent;

    // Use default padding if not provided
    padding = padding || 5; 

    // --- Draw the Background (Blue Rectangle) ---
    
    // 2. Set the fill style for the background
    ctx.fillStyle = 'blue'; 
    
    // Calculate the top-left corner of the rectangle
    // Note: The 'y' coordinate for text in canvas is the baseline, 
    // so we subtract the height and padding to get the top edge of the box.
    const rectX = x - padding;
    const rectY = y - metrics.actualBoundingBoxAscent - padding;
    const rectWidth = textWidth + 2 * padding;
    const rectHeight = textHeight + 2 * padding;

    // 3. Draw the rectangle
    ctx.fillRect(rectX, rectY, rectWidth, rectHeight);

    // --- Draw the Foreground (Text) ---
    
    // 4. Set the fill style for the text
    ctx.fillStyle = 'white'; // White text for contrast on blue
    
    // 5. Draw the text (using the original x and y baseline)
    ctx.fillText(text, x, y);
    
    // 6. Restore the canvas state (reverts colors/fonts to what they were before)
    ctx.restore();
  }
};

const renderUtil = {
  // render specific utils
  
  setCtxFont: function(ctx, fontObj) {
    const boldStr = fontObj.bold ? 'bold' : '';
    const italicStr = fontObj.italic ? 'italic' : '';
    const scaledSizePx = fontObj.sizePx * editorInfo.zoomScale;
    ctx.font = `${boldStr} ${italicStr} ${scaledSizePx}px ${fontObj.name}`;
    ctx.fillStyle = fontObj.color;
  },
  
  setCtxFontStyleOnly: function(ctx, fontObj) {
    ctx.fillStyle = fontObj.color;
  },
  
  getColumnName: function(colNo) {
    return String.fromCharCode('A'.charCodeAt(0)+colNo);
  },

  getColumnWidth: function(colNo) {
    const colMeta = colNo < columnsMetaInfo.length ? columnsMetaInfo[colNo] : null;
    const colWidth = colMeta ? colMeta.width : null;
    return colWidth ? colWidth : metaInfo.defaultColumnWidth;
  }
};