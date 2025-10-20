# Guide d'utilisation - Service de Sanitization XSS

## Vue d'ensemble

Le `SanitizationService` fournit une protection complète contre les attaques XSS (Cross-Site Scripting) en nettoyant toutes les entrées utilisateur avant leur traitement ou stockage.

## Principes de base

### Que faut-il nettoyer ?
- **Tous les champs texte** (input, textarea)
- **Les emails** (avec validation spécifique)
- **Les URLs** (avec validation de protocole)
- **Les mots de passe** (JAMAIS - ils peuvent contenir des caractères spéciaux légitimes)
- **Les booléens et nombres** (pas nécessaire)

### Quand nettoyer ?
- **Avant l'envoi au backend** (dans `onSubmit()`)
- **Lors de l'affichage** (si les données viennent d'une source non fiable)
- **Lors du chargement** (décoder pour l'édition)

---

## Installation

### 1. Créer le service
Placez `sanitization.service.ts` dans `src/app/services/`

### 2. Injecter dans vos composants
```typescript
constructor(
    private sanitizationService: SanitizationService,
    // ... autres services
) { }
```

---

## Méthodes disponibles

### `sanitizeString(value: string): string`
Nettoie une chaîne simple (input text, select, etc.)

**Usage:**
```typescript
const clean = this.sanitizationService.sanitizeString(userInput);
```

**Ce qu'elle fait:**
- Supprime les balises `<script>`, `<iframe>`, etc.
- Échappe les caractères HTML (`<`, `>`, `"`, `'`, `/`)
- Supprime les événements inline (`onclick`, etc.)

---

### `sanitizeMultiline(value: string): string`
Nettoie une chaîne multiligne en préservant les sauts de ligne

**Usage:**
```typescript
const clean = this.sanitizationService.sanitizeMultiline(description);
```

**Idéal pour:** `<textarea>`

---

### `sanitizeEmail(value: string): string`
Nettoie et valide un email

**Usage:**
```typescript
const clean = this.sanitizationService.sanitizeEmail(email);
// Retourne '' si l'email est invalide
```

---

### `sanitizeUrl(value: string): string`
Valide et nettoie une URL

**Usage:**
```typescript
const clean = this.sanitizationService.sanitizeUrl(url);
// Retourne '' si l'URL est dangereuse (javascript:, data:, etc.)
```

---

### `sanitizeFormData<T>(formData: T, config?: {...}): T`
**⭐ LA MÉTHODE PRINCIPALE** - Nettoie automatiquement tout un objet de formulaire

**Usage:**
```typescript
const sanitized = this.sanitizationService.sanitizeFormData(this.form.value, {
    multilineFields: ['description', 'comments'],  // textarea
    emailFields: ['email', 'contactEmail'],        // emails
    urlFields: ['website', 'linkedin'],            // URLs
    skipFields: ['password', 'isActive']           // à ignorer
});
```

---

### `containsDangerousContent(value: string): boolean`
Détecte si une chaîne contient du contenu potentiellement dangereux

**Usage:**
```typescript
if (this.sanitizationService.containsDangerousContent(userInput)) {
    alert('Contenu dangereux détecté !');
    return;
}
```

---

### `decodeHtml(value: string): string`
Décode les entités HTML (pour l'édition de données déjà encodées)

**Usage:**
```typescript
// Lors du chargement pour édition
const decoded = this.sanitizationService.decodeHtml(job.position);
this.form.patchValue({ position: decoded });
```

---

## Exemples complets

### Exemple 1: Formulaire simple (création)

```typescript
onSubmit(): void {
    if (this.form.invalid) return;

    // Nettoyer toutes les données
    const sanitized = this.sanitizationService.sanitizeFormData(
        this.form.value,
        {
            multilineFields: ['description'],
            skipFields: ['isActive']
        }
    );

    // Envoyer au backend
    this.service.create(sanitized).subscribe(...);
}
```

---

### Exemple 2: Formulaire avec édition (update)

```typescript
// Lors du chargement
private initForm(data: Job | null): void {
    this.form = this.fb.group({
        position: [
            data?.position 
                ? this.sanitizationService.decodeHtml(data.position)
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

// Lors de la soumission
onSubmit(): void {
    const sanitized = this.sanitizationService.sanitizeFormData(
        this.form.value,
        { multilineFields: ['description'] }
    );

    this.service.update(this.id, sanitized).subscribe(...);
}
```

---

### Exemple 3: Formulaire avec validation supplémentaire

```typescript
onSubmit(): void {
    if (this.form.invalid) return;

    const sanitized = this.sanitizationService.sanitizeFormData(
        this.form.value,
        {
            multilineFields: ['description'],
            emailFields: ['email'],
            skipFields: ['password']
        }
    );

    // Validation supplémentaire
    const hasDanger = Object.values(sanitized).some(value => 
        typeof value === 'string' && 
        this.sanitizationService.containsDangerousContent(value)
    );

    if (hasDanger) {
        alert('Contenu invalide détecté !');
        return;
    }

    // Vérifier que l'email est toujours valide
    if (!sanitized.email) {
        this.error = 'Email invalide';
        return;
    }

    this.service.create(sanitized).subscribe(...);
}
```

---

### Exemple 4: Formulaire de recherche

```typescript
onSearch(): void {
    const sanitized = this.sanitizationService.sanitizeFormData(
        this.searchForm.value,
        {
            skipFields: ['sort', 'page', 'size', 'isActive'] // booleans/numbers
        }
    );

    this.service.search(sanitized).subscribe(...);
}
```

---

## Points d'attention

### NE JAMAIS nettoyer les mots de passe
```typescript
// MAUVAIS
const sanitized = this.sanitizationService.sanitizeString(password);

//  BON
const sanitized = this.sanitizationService.sanitizeFormData(formValue, {
    skipFields: ['password', 'passwordConfirm']
});
```

###  Toujours décoder avant édition
```typescript
// Pour permettre à l'utilisateur d'éditer correctement
this.form.patchValue({
    name: this.sanitizationService.decodeHtml(existingData.name)
});
```

###  Vérifier les emails après nettoyage
```typescript
const sanitized = this.sanitizationService.sanitizeEmail(email);
if (!sanitized || sanitized === '') {
    // Email invalide
    return;
}
```

---

## Checklist pour chaque formulaire

- [ ] Service injecté dans le constructeur
- [ ] `sanitizeFormData()` appelé dans `onSubmit()`
- [ ] Configuration appropriée (multiline, email, skip)
- [ ] Mots de passe dans `skipFields`
- [ ] Validation des emails après sanitization
- [ ] Décodage avec `decodeHtml()` lors du chargement pour édition
- [ ] (Optionnel) Vérification avec `containsDangerousContent()`

---

## Sécurité en profondeur

Ce service est votre **première ligne de défense** côté frontend, mais :

1.  Le backend DOIT aussi valider/sanitizer
2.  Utilisez des requêtes préparées (PreparedStatements) côté backend
3.  Configurez les CSP (Content Security Policy) headers
4.  Utilisez HTTPS
5.  Validez les types de données côté backend

---

## Performance

Le service est **très léger** et n'impacte pas les performances :
- Traitement synchrone
- Pas de dépendances externes
- Optimisé pour les formulaires de taille moyenne

Pour des formulaires très volumineux (>100 champs), envisagez un traitement asynchrone.

---

## Support

Pour toute question ou amélioration, consultez :
- Documentation Angular : https://angular.io/guide/security
- OWASP XSS Prevention : https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html
