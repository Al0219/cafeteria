import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  readonly loginForm = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  readonly isSubmitting = signal(false);
  readonly isDisabled = computed(() => this.isSubmitting() || this.loginForm.invalid);

  constructor(private readonly fb: FormBuilder,
              private readonly auth: AuthService,
              private readonly router: Router) {}

  ngOnInit(): void {
    if (this.auth.isLoggedIn()) {
      this.router.navigate([this.auth.homeRoute()]);
    }
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const payload = {
      usernameOrEmail: this.loginForm.controls.username.value,
      password: this.loginForm.controls.password.value
    };
    this.auth.login(payload).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.router.navigate([this.auth.homeRoute()]);
      },
      error: err => {
        this.isSubmitting.set(false);
        const msg = (err?.error && (err.error.message || err.error.error)) || 'Credenciales inválidas';
        alert(msg);
      }
    });
  }
}

