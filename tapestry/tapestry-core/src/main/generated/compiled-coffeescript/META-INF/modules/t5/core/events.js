(function() {
  define({
    form: {
      validate: "t5:form:validate",
      prepareForSubmit: "t5:form:prepare-for-submit"
    },
    field: {
      optional: "t5:field:optional",
      translate: "t5:field:translate",
      validate: "t5:field:validate",
      inputValidation: "t5:field:input-validation",
      clearValidationError: "t5:field:clear-validation-error",
      showValidationError: "t5:field:show-validation-error"
    },
    palette: {
      willChange: "t5:palette:willChange",
      didChange: "t5:palette:didChange"
    },
    zone: {
      update: "t5:zone:update",
      willUpdate: "t5:zone:will-update",
      didUpdate: "t5:zone:did-update",
      refresh: "t5:zone:refresh"
    },
    element: {
      didShow: "t5:element:did-show",
      didHide: "t5:element:did-hide"
    },
    formfragment: {
      changeVisibility: "t5:fragment:change-visibility"
    }
  });

}).call(this);
