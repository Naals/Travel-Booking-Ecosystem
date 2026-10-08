{{/* Every values/<service>.yaml sets nameOverride to the service's own name — this is how one chart installs as 14 distinct releases. */}}
{{- define "travel-service.name" -}}
{{- .Values.nameOverride | default .Release.Name -}}
{{- end -}}

{{- define "travel-service.image" -}}
{{- printf "%s/%s:%s" .Values.image.registry (include "travel-service.name" .) .Values.image.tag -}}
{{- end -}}

{{- define "travel-service.labels" -}}
app: {{ include "travel-service.name" . }}
app.kubernetes.io/name: {{ include "travel-service.name" . }}
app.kubernetes.io/part-of: travel-platform
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}
