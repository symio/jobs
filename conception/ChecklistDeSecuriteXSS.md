# Checklist de sécurité XSS - Protection complète

## Mise en place initiale

### Étape 1: Créer le service
- [ ] Créer `src/app/services/sanitization.service.ts`
- [ ] Copier le code du service complet
- [ ] Créer `src/app/services/sanitization.service.spec.ts` (tests)

### Étape 2: Configuration globale
- [ ] Vérifier que le service est `providedIn: 'root'`
- [ ] Pas besoin de l'ajouter dans `providers` (standalone service)

---

## Pour chaque formulaire

### A. Injection du service
```typescript
constructor(
    private sanitizationService: SanitizationService,
    // ... autres services
) { }
```

### B. Identification des types de champs
- [ ] Lister tous les champs du formulaire
- [ ] Identifier les `<textarea>` → `multilineFields`
- [ ] Identifier les emails → `emailFields`
- [ ] Identifier les URLs → `urlFields`
- [ ] Identifier ce qui ne doit PAS être sanitizé → `skipFields`
  - Mots de passe
  - Booléens
  - Nombres
  - Dates

### C. Implémentation dans `onSubmit()`

```typescript
onSubmit(): void {
    if (this.form.invalid) return;

    // 1 Sanitization
    const sanitized = this.sanitizationService.sanitizeFormData(
        this.form.value,
        {
            multilineFields: ['description', 'comments'],
            emailFields: ['email'],
            urlFields: ['website'],
            skipFields: ['password', 'isActive']
        }
    );

    // 2 Validation supplémentaire (optionnel mais recommandé)
    if (this.hasValidationErrors(sanitized)) {
        return;
    }

    // 3 Envoi au backend
    this.service.create(sanitized).subscribe(...);
}

private hasValidationErrors(data: any): boolean {
    // Vérifier les contenus dangereux
    const hasDanger = Object.entries(data)
        .filter(([key]) => !['password'].includes(key))
        .some(([_, value]) => 
            typeof value === 'string' && 
            this.sanitizationService.containsDangerousContent(value)
        );

    if (hasDanger) {
        this.error = 'Contenu invalide détecté';
        return true;
    }

    // Vérifier les emails
    if (data.email && data.email === '') {
        this.error = 'Email invalide';
        return true;
    }

    return false;
}
```

### D. Pour les formulaires avec édition (UPDATE)

```typescript
// Lors du chargement
private initForm(data: Model | null): void {
    this.form = this.fb.group({
        name: [
            data?.name 
                ? this.sanitizationService.decodeHtml(data.name)
                : '',
            Validators.required
        ],
        description: [
            data?.description
                ? this.sanitizationService.decodeHtml(data.description)
                : ''
        ]
    });
}
```

---

## Cas d'usage spécifiques

### Formulaire de création (CREATE)
```typescript
 Sanitization avant envoi
 Pas de décodage nécessaire
```

### Formulaire d'édition (UPDATE)
```typescript
 Décodage lors du chargement (decodeHtml)
 Sanitization avant envoi
```

### Formulaire de recherche (SEARCH)
- Décodage optionnel si les données proviennent du backend
- Sanitization systématique avant exécution de la recherche
- Pas de modification des filtres numériques ou booléens

### Méthodes disponibles dans `SanitizationService`

#### 1. `sanitizeString(value: string | null | undefined): string`
Nettoie une chaîne en supprimant les balises HTML dangereuses, les attributs `on*`, échappant les caractères spéciaux et normalisant les espaces.

**Exemple :**
```typescript
const clean = this.sanitizationService.sanitizeString('<script>alert(1)</script> Hello');
// "Hello"
```

#### 2. sanitizeMultiline(value: string | null | undefined): string
Similaire à sanitizeString mais conserve les sauts de ligne.

**Exemple :**
```typescript
const clean = this.sanitizationService.sanitizeMultiline("Hello\n<script>alert(1)</script>\nWorld");
// "Hello\nWorld"
```

#### 3. sanitizeObject<T>(obj: T, multilineFields: string[] = []): T

Sanitization récursive d’un objet complet, incluant les objets imbriqués et tableaux. 

Les champs listés dans multilineFields utilisent sanitizeMultiline.

#### 4. sanitizeUrl(url: string | null | undefined): string

Filtre les URLs dangereuses (javascript:, data:, etc.), autorise HTTP(S), 
chemins relatifs ou ancres, sinon retourne une chaîne vide.

**Exemple :**
```typescript
this.sanitizationService.sanitizeUrl('javascript:alert(1)'); // ''
this.sanitizationService.sanitizeUrl('https://example.com'); // 'https://example.com'
```

#### 5. sanitizeEmail(email: string | null | undefined): string
Nettoie et valide un email. Supprime < > " ' et convertit en minuscule. 
Retourne une chaîne vide si invalide.

#### 6. decodeHtml(value: string): string
Décode les entités HTML (&lt;, &gt;, etc.) pour affichage.

#### 7. containsDangerousContent(value: string | null | undefined): boolean
Vérifie la présence de balises ou attributs dangereux et retourne true si détectés.

#### 8. sanitizeFormData<T>(formData: T, config?): T
Sanitization complète d’un formulaire avant soumission.

Paramètres optionnels :
- multilineFields : champs textarea
- emailFields : champs email
- urlFields : champs URL
- skipFields : champs à ignorer (mot de passe, booléens, nombres)

**Exemple :**
```typescript
const sanitized = this.sanitizationService.sanitizeFormData(form.value, {
    multilineFields: ['description'],
    emailFields: ['email'],
    urlFields: ['website'],
    skipFields: ['password']
});
```

### Bonnes pratiques

- Toujours sanitiser avant envoi au backend
- Ne pas décoder avant envoi (sauf affichage dans un formulaire d’édition)
- Pour multi-lignes, utiliser sanitizeMultiline
- Vérifier le contenu dangereux avec containsDangerousContent
- Filtrer URLs et emails avant enregistrement

### Tests recommandés
- Vérifier suppression des balises dangereuses (<script>, <iframe>, etc.)
- Vérifier échappement des caractères spéciaux
- Vérifier conservation des sauts de ligne pour sanitizeMultiline
- Vérifier objets imbriqués et tableaux pour sanitizeObject
- Tester validation d’URLs et d’emails
