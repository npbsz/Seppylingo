import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TtsButtonComponent } from './tts-button.component';

describe('TtsButtonComponent', () => {
  let component: TtsButtonComponent;
  let fixture: ComponentFixture<TtsButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TtsButtonComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TtsButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
